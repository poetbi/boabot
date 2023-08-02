package cn.boasoft.boabot;

import static cn.boasoft.boabot.library.tool.md5;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.library.app;
import cn.boasoft.boabot.library.http;
import cn.boasoft.boabot.library.phone;
import cn.boasoft.boabot.library.sqlite;

public class robot extends AccessibilityService {
    private final String[] packages = {"cn.boasoft.boa"};
    private sqlite db;
    private ArrayList<Long> queue = new ArrayList<>();
    private ArrayList<Bundle> flows = new ArrayList<>(); //工作流内容
    private int index = 0;  //工作流执行索引
    private int retry = 0;  //失败重试次数
    private int status = 0; //执行状态
    private int gotoNum = 0; //跳转计数器
    private boolean gotoEnd = false; //跳转结束
    private boolean inTest = false; //测试模式
    private ExecutorService excutor;
    private String[] start = new String[]{"app", ""};
    private HashMap<Long, String> params = new HashMap<>();
    private app app;
    private List<AccessibilityNodeInfo> page_eles = new ArrayList<>(); //当前页查到元素集合
    private int page_i = 0; //当前页执行索引
    private int page_top; //当前页顶部
    private int page_bot; //当前页底部
    private boolean skip = false; //是否忽略日志

    @Override
    public void onCreate(){
        app = new app(robot.this);
    }

    @Override
    public void onServiceConnected(){
		super.onServiceConnected();
        config();
        EventBus.getDefault().register(this);
        excutor();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkgName = (String) event.getPackageName();
        if(status == 0 && !pkgName.equals("cn.boasoft.boabot")) {
            AccessibilityNodeInfo ele = event.getSource();
            if(ele != null) {
                excutor.submit(new doChoose(ele));
            }
        }
    }

    @Override
    public void onInterrupt() {
        if(excutor != null)  excutor.shutdownNow();
        EventBus.getDefault().unregister(this);
    }

    private void queue(){
        if(queue.size() > 0){
            if(db == null){
                db = new sqlite(robot.this);
            }
            long tid = queue.get(0);
            flows = db.getFlowsByTid(tid);
            if(flows != null && flows.size() > 0){
				db.setLog(0, 0, "开启任务："+ tid);
                db.editTaskLast(tid);
                task();
                //此后代码执行不到
            }else{
                nextTask(9);
            }
        }else{ //队列完成
            status = 0;
            if(db != null){
                db.close();
                db = null;
            }
        }
    }

    private void task() {
        Bundle flow = flows.get(index);
        long fid = flow.getLong("id");
        String cmd = flow.getString("cmd");
        String obj = flow.getString("obj");
        String val = flow.getString("val");
        String ctl = flow.getString("ctl");
        String[] arr = ctl.split(",");
        int loop = Integer.parseInt(arr[0]);
        long wait = Long.parseLong(arr[1]) * 1000L;
        int step = Integer.parseInt(arr[2]);
        if(inTest) wait += 3000L;
        boolean result = false;
        int res = -1;

        switch (cmd){
            case "start":
                start[0] = obj;
                start[1] = val;
                res = app.run(obj, val);
                sleep(wait);
                break;

            case "click":
                sleep(wait);
                if(!obj.isEmpty() && obj.charAt(0) == '*'){
                    String pos = obj.substring(1, obj.length() - 2);
                    result = pointClick(pos, val);
                    res = result ? 0 : 6;
                } else {
                    int type = Integer.parseInt(val);
                    if(type > 1) {
                        if(type == 2){
                            for (int i = 0; i < loop; i++) {
                                result = performGlobalAction(GLOBAL_ACTION_BACK);
                                sleep(1000L);
                            }
                        }else if(type == 3){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                result = performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
                            } else {
                                result = true;
                            }
                        }
                        res = result ? 0 : 4;
                    }else{
                        AccessibilityNodeInfo ele = find(obj, loop);
                        if (ele == null) {
                            res = 3;
                            break;
                        } else {
                            if (type == 1) {
                                result = ele.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                            } else {
                                result = ele.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            res = result ? 0 : 4;
                        }
                    }
                }
                break;

            case "input":
                sleep(wait);
                if(!val.isEmpty() && val.charAt(0) == '@'){
                    if(!val.contains("?")){
                        val += "?retry="+ retry;
                    }else{
                        val += "&retry="+ retry;
                    }
                    http http = new http();
                    val = http.get(val.substring(1));
                }

                if(val == null){
                    res = 5;
                }else{
                    long tid = queue.get(0);
                    if (params.containsKey(tid)) {
                        String param = params.get(tid);
                        if (param != null && !param.isEmpty()) {
                            arr = param.split("\\|");
                            for (int i = 0; i < arr.length; i++) {
                                val = val.replace("{BOA" + i + "}", arr[i]);
                            }
                            val = val.replace("{BOA}", param);
                        }
                    }

                    if(!obj.isEmpty() && obj.charAt(0) == '*'){
                        String pos = obj.substring(1, obj.length() - 2);
                        result = pointClick(pos, val);
                        if (result) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("val", val);
                            clipboard.setPrimaryClip(clip);
                            result = performGlobalAction(AccessibilityNodeInfo.ACTION_PASTE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                clipboard.clearPrimaryClip();
                            }
                        }
                        res = result ? 0 : 7;
                    } else {
                        AccessibilityNodeInfo ele = find(obj, loop);
                        if (ele == null) {
                            res = 3;
                        } else {
                            Bundle args = new Bundle();
                            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, val);
                            result = ele.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                            args.clear();

                            if (!result) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("val", val);
                                clipboard.setPrimaryClip(clip);
                                ele.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                                result = ele.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    clipboard.clearPrimaryClip();
                                }
                            }
                            res = result ? 0 : 4;
                        }
                    }
                }
                break;

            case "check":
                sleep(wait);
                AccessibilityNodeInfo ele = find(obj, 0);
                arr = val.split(":");
                int check_obj = Integer.parseInt(arr[0]);
                int check_goto = Integer.parseInt(arr[1]);
                if(check_obj == 0 && ele == null){
                    index = check_goto - 1;
                }else if(check_obj == 1 && ele != null){
                    index = check_goto - 1;
                }
                res = 0;
                break;

            case "goto":
                sleep(wait);
                if((loop > 0 && gotoNum < loop) || !gotoEnd) {
                    if (obj.equals("0")) {
                        int flow_i = Integer.parseInt(val);
                        index = flow_i - 1;
                    } else {
                        long tid = Long.parseLong(val);
                        if (tid > 0) queue.add(tid);
                    }
                    gotoNum++;
                }else{
                    gotoNum = 0;
                    gotoEnd = false;
                }
                res = 0;
                break;

            case "close":
                if(start[0].equals("app") && !start[1].isEmpty()) {
                    val = start[1];
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    try {
                        Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
                        method.invoke(am, val);
                    } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException ignored) {
                        am.killBackgroundProcesses(val);
                    }
                }
                res = 0;
                break;
        }
        if(!skip) db.setLog(fid, res, val);

        if( inTest    || //测试模式
            res <= 0  || //执行正确
            step == 1 || //可选步骤
            retry > 3    //重试3次
        ) {
            retry = 0;
            index++;
        }else{
            retry++;
            sleep(retry * 1000L);
        }
        skip = false;

        if(index >= flows.size()) { //此任务完成
            nextTask(0);
        }else{
            task();
        }
    }

    private void activeApp(){
        if(start[0].equals("app") && !start[1].isEmpty()){
            app.run(start[0], start[1]);
        }
    }

    private void nextTask(int res){
        index = 0;
        flows.clear();
        params.clear();
        start = new String[]{"app", ""};
        db.setLog(0, res, "结束任务："+ queue.get(0));
        queue.remove(0);
        queue();
    }

    private void sleep(long wait){
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private AccessibilityNodeInfo find(String obj, int foreach){
        if(obj == null || obj.isEmpty()) return null;

        int i;
        int j;
        String id;
        AccessibilityNodeInfo ele;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        String[] arr = obj.split("#", 3);
        String[] subarr;
        char type = arr[0].charAt(0);
        String reg = arr[0].substring(1);
        String mode;
        if(reg.indexOf('>') > 0){
            mode = ">";
            subarr = reg.split(mode);
        }else if(reg.indexOf('<') > 0){
            mode = "<";
            subarr = reg.split(mode);
        }else{
            mode = "=";
            subarr = new String[]{reg};
        }
        if(foreach == 1){
            i = page_i;
        }else {
            i = Integer.parseInt(subarr[0]);
        }
        id = arr[1];

        if(foreach == 1 && page_i == 0) {
            String md5_last = null;
            if(page_eles.size() > 0) {
                try {
                    md5_last = md5(page_eles.get(page_eles.size() - 1).toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            activeApp();
            if (type == '@') {
                page_eles = root.findAccessibilityNodeInfosByText(id);
            } else {
                page_eles = root.findAccessibilityNodeInfosByViewId(id);
            }

            String new_md5_first = null, new_md5_last = null;
            try {
                new_md5_first = md5(page_eles.get(0).toString());
                new_md5_last = md5(page_eles.get(page_eles.size() - 1).toString());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if(new_md5_last != null && new_md5_last.equals(md5_last)){
                gotoEnd = true; //最后一页执行两次，和前一次判断相同便为到底了
                page_eles.clear();
                skip = true;
                return null;
            }
            if(new_md5_first != null && new_md5_first.equals(md5_last)){
                skip = true;
                return null; //判重
            }
        }else{
            activeApp();
            if (type == '@') {
                page_eles = root.findAccessibilityNodeInfosByText(id);
            } else {
                page_eles = root.findAccessibilityNodeInfosByViewId(id);
            }
        }
        if(page_eles.size() < 1) return null;

        switch (mode){
            case "<" :
                ele = page_eles.get(i);
                if(ele != null) {
                    int depth = 0;
                    for (j = 1; j < subarr.length; j++) {
                        depth += Integer.parseInt(subarr[j]);
                    }
                    for (j = 0; j < depth; j++) {
                        if(ele != null) {
                            ele = ele.getParent();
                        }
                    }
                }
                break;

            case ">" :
                ele = page_eles.get(i);
                if(ele != null) {
                    for (j = 1; j < subarr.length; j++) {
                        int no = Integer.parseInt(subarr[j]);
                        if(ele != null){
                            ele = ele.getChild(no);
                        }
                    }
                }
                break;

            default:
                ele = page_eles.get(i);
                break;
        }

        if(foreach == 1){
            if(i == 0){
                Rect rect = new Rect();
                ele.getBoundsInScreen(rect);
                page_top = rect.top;
            }
            if(i >= page_eles.size() - 1){
                page_i = 0;

                Rect rect = new Rect();
                ele.getBoundsInScreen(rect);
                page_bot = rect.bottom;

                int height = page_bot - page_top;
                slideUp(height);
            }else {
                page_i++;
            }
        }
        return ele;
    }

    private Point convert(String val){
        String[] arr = val.split(",");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int x = Integer.parseInt(arr[0]) / metrics.widthPixels * Integer.parseInt(arr[2]);
        int y = Integer.parseInt(arr[1]) / metrics.heightPixels * Integer.parseInt(arr[3]);
        return new Point(x, y);
    }

    private boolean pointClick(String obj, String val){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Point point = convert(obj);
            Path path = new Path();
            path.moveTo(point.x, point.y);
            int clickTime = val.equals("1") ? 600 : 1;
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(new GestureDescription.StrokeDescription(path, 0, clickTime))
                    .build();
            activeApp();
            return dispatchGesture(gesture, null, null);
        }else{
            return false;
        }
    }

    private void slideUp(int height){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int x = metrics.widthPixels / 2;
            int y = metrics.heightPixels / 2;
            int num = 1;

            if(height > y){
                height = height / 2;
                num = 2;
            }else if(height < 100){
                height = 100;
            }

            for(int i = 0; i < num; i++) {
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x, y - height);
                GestureDescription gesture = new GestureDescription.Builder()
                        .addStroke(new GestureDescription.StrokeDescription(path, 100, 500))
                        .build();
                dispatchGesture(gesture, null, null);
                sleep(500L);
            }
        }
    }

    private void config() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.packageNames = packages;
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
    }

    @Subscribe
    public void handleEvent(event e) {
        switch (e.type){
            case "execute":
                inTest = false;
                execute(e);
                break;

            case "package":
                packages[0] = e.data;
                config();
                break;

            case "test":
                inTest = true;
                execute(e);
                break;

            case "stop":
                inTest = false;
                flows.clear();
                queue.clear();
                break;

            case "phone":
                if(status == 1){
                    excutor.submit(new doEndCall());
                }
                break;
        }
    }

    private void execute(event e){
        long tid = Long.parseLong(e.data);
        if(e.more != null) {
            params.put(tid, e.more);
        }

        if (!queue.contains(tid)) {
            queue.add(tid);
            if (status == 0) {
                status = 1;
                excutor.submit(new doTask());
            }
        }
    }

    private void excutor(){
        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(32);
        excutor = new ThreadPoolExecutor(1, 10, 1L, TimeUnit.SECONDS, blockingQueue);
    }

    private int findIndex(String md5, List<AccessibilityNodeInfo> nodes){
        int res = -1;
        String node_md5 = null;
        AccessibilityNodeInfo node;
        int num = nodes.size();

        for (int i = 0; i < num; i++) {
            node = nodes.get(i);
            if(node != null) {
                try {
                    node_md5 = md5(node.toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (md5 != null && md5.equals(node_md5)) return i;
            }
        }
        return res;
    }

    private String findIdDn(AccessibilityNodeInfo ele){
        String id = null;
        AccessibilityNodeInfo node;
        int num = ele.getChildCount();

        for (int i = 0; i < num; i++) {
            node = ele.getChild(i);
            if(node != null) {
                id = node.getViewIdResourceName();
                if (id != null)  return id;
            }
        }
        return id;
    }

    private String findTextDn(AccessibilityNodeInfo ele){
        String text = null;
        AccessibilityNodeInfo node;
        int num = ele.getChildCount();

        for (int i = 0; i < num; i++) {
            node = ele.getChild(i);
            if(node != null) {
                text = (String) node.getText();
                if (text != null && !text.isEmpty()) return text;
            }
        }
        return text;
    }

    private int findIndexDn(String md5, AccessibilityNodeInfo ele){
        int res = -1;
        String node_md5 = null;
        AccessibilityNodeInfo node;
        int num = ele.getChildCount();

        for (int i = 0; i < num; i++) {
            node = ele.getChild(i);
            if(node != null) {
                try {
                    node_md5 = md5(node.toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (md5 != null && md5.equals(node_md5)) return i;
            }
        }
        return res;
    }

    @SuppressLint("DefaultLocale")
    private void choose(AccessibilityNodeInfo ele){
        int i;
        int num;
        String md5 = null;
        AccessibilityNodeInfo node;
        List<AccessibilityNodeInfo> nodes;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        try {
            md5 = md5(ele.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String obj = null;
        String id = ele.getViewIdResourceName();
        String text = (String) ele.getText();
        if (id != null) {
            nodes = root.findAccessibilityNodeInfosByViewId(id);
            num = nodes.size();
            if (num > 1) {
                i = findIndex(md5, nodes);
                if(i > -1){ //id查找结果第i+1个元素
                    obj = "$"+ i +"#"+ id;
                }
            }else{ //id查找结果首个元素
                obj = "$0#"+ id;
            }
        }else{
            String nid = findIdDn(ele);
            if(nid != null){ //nid查找结果首个元素向上1层
                obj = "$0<1#"+ nid;
            }else{
                node = ele.getParent();
                if(node != null) {
                    nid = node.getViewIdResourceName();
                    if (nid != null) {
                        i = findIndexDn(md5, ele.getParent());
                        if (i > -1) { //nid查找结果首个元素第i+1子元素
                            obj = "$0>" + i + "#" + nid;
                        }
                    }
                }
            }
        }

        if(obj == null){
            if (text != null && !text.isEmpty()){
                nodes = root.findAccessibilityNodeInfosByText(text);
                num = nodes.size();
                if(num > 1){
                    i = findIndex(md5, nodes);
                    if(i > -1){
                        obj = "@="+ i +"#"+ text;
                    }
                }else{
                    obj = "@=0#"+ text;
                }
            }else{
                String ntext = findTextDn(ele);
                if(ntext != null && !ntext.isEmpty()){
                    obj = "@<0#"+ ntext;
                }else{
                    node = ele.getParent();
                    if(node != null) {
                        ntext = (String) node.getText();
                        if (ntext != null) {
                            i = findIndexDn(md5, node);
                            if (i > -1) {
                                obj = "@>" + i + "#" + ntext;
                            }
                        }
                    }
                }
            }
        }

        if (obj == null) {
            if(id != null && !id.isEmpty()){
                obj = "$=0#"+ id;
            }else if(text != null && !text.isEmpty()){
                obj = "@=0#"+ text;
            }
        }

        if (obj == null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Rect rect = new Rect();
            ele.getBoundsInScreen(rect);
            text = String.format("%d,%d,%d,%d", metrics.widthPixels, metrics.heightPixels, rect.centerX(), rect.centerY());
            obj = "*" + text;
        }

        if (obj != null) {
            int len = 30;
            if(text == null || text.isEmpty() || text.equals("null")) text = id;
            if(text != null && text.length() > len) {
                text = text.substring(0, len);
            }
            EventBus.getDefault().post(new event("callback", obj +"#0", text));
        }
    }

    private class doChoose implements Callable<Integer>{
        AccessibilityNodeInfo ele;
        public doChoose(AccessibilityNodeInfo ele){
            this.ele = ele;
        }

        @Override
        public Integer call() {
            choose(ele);
            return 0;
        }
    }

    private class doTask implements Callable<Integer>{
        @Override
        public Integer call() {
            queue();
            return 0;
        }
    }

    private class doEndCall implements Callable<Integer>{
        @Override
        public Integer call() {
            new phone(robot.this).stop();
            return 0;
        }
    }
}