package cn.boasoft.boabot.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class sqlite extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String NAME = "boabot.db";
    private SQLiteDatabase dbw;
    private SQLiteDatabase dbr;

    public sqlite(@Nullable Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE task(id INTEGER PRIMARY KEY AUTOINCREMENT, status INTEGER, name VARCHAR(100), type INTEGER DEFAULT 0, exec VARCHAR(50), time INTEGER, last INTEGER DEFAULT 0, auth INTEGER DEFAULT 1)");
        db.execSQL("CREATE INDEX status ON task (status)");
        db.execSQL("CREATE INDEX last ON task (last)");

        db.execSQL("CREATE TABLE flow(id INTEGER PRIMARY KEY AUTOINCREMENT, tid INTEGER, cmd VARCHAR(10), obj VARCHAR(200), val TEXT, ctl VARCHAR(20), memo VARCHAR(100), sort INTEGER DEFAULT 0)");
        db.execSQL("CREATE INDEX f_tid ON flow (tid)");
        db.execSQL("CREATE INDEX cmd ON flow (cmd)");

        db.execSQL("CREATE TABLE log(id INTEGER PRIMARY KEY AUTOINCREMENT, fid INTEGER, res INTEGER, val TEXT, time INTEGER)");
        db.execSQL("CREATE INDEX l_time ON log (time)");

        db.execSQL("CREATE TABLE timer(id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, tid INTEGER)");
        db.execSQL("CREATE INDEX t_time ON timer (time)");
        db.execSQL("CREATE INDEX t_tid ON timer (tid)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private Bundle item_task(Cursor rs){
        Bundle item = new Bundle();
        item.putLong("id", rs.getLong(0));
        item.putInt("status", rs.getInt(1));
        item.putString("name", rs.getString(2));
        item.putInt("type", rs.getInt(3));
        item.putString("exec", rs.getString(4));
        item.putLong("time", rs.getLong(5));
        item.putLong("last", rs.getLong(6));
        item.putInt("auth", rs.getInt(7));
        return item;
    }

    private ContentValues val_task(Bundle data){
        ContentValues val = new ContentValues();
        val.put("status", data.getInt("status"));
        val.put("name", data.getString("name"));
        val.put("type", data.getInt("type"));
        val.put("exec", data.getString("exec"));
        val.put("time", System.currentTimeMillis());
        return val;
    }

    private Bundle item_flow(Cursor rs){
        Bundle item = new Bundle();
        item.putLong("id", rs.getLong(0));
        item.putLong("tid", rs.getLong(1));
        item.putString("cmd", rs.getString(2));
        item.putString("obj", rs.getString(3));
        item.putString("val", rs.getString(4));
        item.putString("ctl", rs.getString(5));
        item.putString("memo", rs.getString(6));
        item.putInt("sort", rs.getInt(7)); //大->小
        return item;
    }

    private ContentValues val_flow(Bundle data){
        ContentValues val = new ContentValues();
        if(data.containsKey("tid")) {
            val.put("tid", data.getLong("tid"));
        }
        val.put("cmd", data.getString("cmd"));
        val.put("obj", data.getString("obj"));
        val.put("val", data.getString("val"));
        val.put("ctl", data.getString("ctl"));
        val.put("memo", data.getString("memo"));
        return val;
    }

    private Bundle item_log(Cursor rs){
        Bundle item = new Bundle();
        item.putLong("id", rs.getLong(0));
        item.putLong("fid", rs.getLong(1));
        item.putInt("res", rs.getInt(2));
        item.putString("val", rs.getString(3));
        item.putLong("time", rs.getLong(4));

        item.putLong("tid", rs.getLong(5));
        item.putString("memo", rs.getString(6));
        return item;
    }

    private Bundle item_timer(Cursor rs){
        Bundle item = new Bundle();
        item.putLong("id", rs.getLong(0));
        item.putLong("time", rs.getLong(1));
        item.putLong("tid", rs.getLong(2));
        return item;
    }

    public long getTaskLastId() {
        long id = 0;
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT id FROM task ORDER BY id DESC LIMIT 1", null);
        try {
            if (rs.moveToNext()) {
                id = rs.getLong(0);
            }
        } finally {
            rs.close();
        }
        return id;
    }

    public ArrayList<Bundle> getTasks() {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT * FROM task ORDER BY id ASC", null);
        try {
            while (rs.moveToNext()) {
                list.add(item_task(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public ArrayList<Bundle> getTasksOn() {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT * FROM task WHERE status = 1 ORDER BY id ASC", null);
        try {
            while (rs.moveToNext()) {
                list.add(item_task(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public ArrayList<Bundle> getTasksLast() {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        long last = System.currentTimeMillis() - 86400 * 1000;
        Cursor rs = db.rawQuery("SELECT * FROM task WHERE last > "+ last +" ORDER BY last DESC", null);
        try {
            while (rs.moveToNext()) {
                list.add(item_task(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public ArrayList<Bundle> getTasksNext() {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        long time = System.currentTimeMillis();
        Cursor rs = db.rawQuery("SELECT B.*, A.time FROM timer A LEFT JOIN task B ON A.tid = B.id WHERE A.time > "+ time +" AND A.tid > 0 ORDER BY A.time ASC", null);
        try {
            while (rs.moveToNext()) {
                Bundle item = item_task(rs);
                item.putLong("time", rs.getLong(8));
                list.add(item);
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public long addTask(Bundle data) {
        SQLiteDatabase db = db(1);
        ContentValues val = val_task(data);
        return db.insert("task",null, val);
    }

    public int editTask(Bundle data){
        SQLiteDatabase db = db(1);
        ContentValues val = val_task(data);
        String id = String.valueOf(data.getLong("id"));
        String[] args = {id};
        return db.update("task", val, "id = ?", args);
    }

    public void editTaskLast(long tid){
        SQLiteDatabase db = db(1);
        ContentValues val = new ContentValues();
        val.put("last", System.currentTimeMillis());
        String id = String.valueOf(tid);
        String[] args = {id};
        db.update("task", val, "id = ?", args);
    }

    public void setTaskAuth(long id, int auth){
        SQLiteDatabase db = db(1);
        db.execSQL("UPDATE task SET auth = "+ auth +" WHERE id = "+ id);
    }

    public void delTask(long id) {
        SQLiteDatabase db = db(1);
        db.execSQL("DELETE FROM flow WHERE tid = "+ id);
        db.execSQL("DELETE FROM task WHERE id = "+ id);
    }

    public String getTaskApp(long tid){
        String app = "";
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT val FROM flow WHERE tid = "+ tid +" AND cmd = 'start' AND obj = 'app' LIMIT 1", null);
        try {
            if (rs.moveToNext()) {
                app = rs.getString(0);
            }
        } finally {
            rs.close();
        }
        return app;
    }

    public Bundle getTaskStart(long tid){
        Bundle list = new Bundle();
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT * FROM flow WHERE tid = "+ tid +" AND cmd = 'start' LIMIT 1", null);
        try {
            if (rs.moveToNext()) {
                list = item_flow(rs);
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public ArrayList<Bundle> getFlowsByTid(long tid){
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        Cursor rs = db.rawQuery("SELECT * FROM flow WHERE tid = "+ tid +" ORDER BY sort DESC, id ASC", null);
        try {
            while (rs.moveToNext()) {
                list.add(item_flow(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public long addFlow(Bundle data){
        SQLiteDatabase db = db(1);
        ContentValues val = val_flow(data);
        return db.insert("flow",null, val);
    }

    public int editFlow(Bundle data){
        SQLiteDatabase db = db(1);
        ContentValues val = val_flow(data);
        String id = String.valueOf(data.getLong("id"));
        String[] args = {id};
        return db.update("flow", val, "id = ?", args);
    }
	
    public void editFlowSort(long[] ids){
        SQLiteDatabase db = db(1);
        int sort;
        int len = ids.length;
        for(int i = 0; i < len; i++){
            sort = len - i;
			db.execSQL("UPDATE flow SET sort = "+ sort +" WHERE id = "+ ids[i]);
		}
    }

    public void delFlow(long id) {
        SQLiteDatabase db = db(1);
        db.execSQL("DELETE FROM flow WHERE id = "+ id);
    }

    public ArrayList<Bundle> getLogs(long page, int size) {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        long offset = (page - 1) * (long) size;
        Cursor rs = db.rawQuery("SELECT A.*, B.tid, B.memo FROM log A LEFT JOIN flow B ON A.fid = B.id ORDER BY A.id DESC LIMIT "+ offset +","+ size, null);
        try {
            while (rs.moveToNext()) {
                list.add(item_log(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public void setLog(long fid, int res, String val){
        SQLiteDatabase db = db(1);
        long time = System.currentTimeMillis();
        db.execSQL("INSERT INTO log (fid, res, val, time) VALUES ("+ fid +", "+ res +", '"+ val +"', "+ time +")");
    }

    public void delLog(long time){
        SQLiteDatabase db = db(1);
        db.execSQL("DELETE FROM log WHERE time < "+ time);
    }

    public ArrayList<Bundle> getTimersCurrent() {
        ArrayList<Bundle> list = new ArrayList<>();
        SQLiteDatabase db = db(0);
        long time = System.currentTimeMillis() + 5 * 1000;
        Cursor rs = db.rawQuery("SELECT * FROM timer WHERE time <= "+ time +" GROUP BY tid ORDER BY id DESC", null);
        try {
            while (rs.moveToNext()) {
                list.add(item_timer(rs));
            }
        } finally {
            rs.close();
        }
        return list;
    }

    public long addTimer(Bundle data){
        SQLiteDatabase db = db(1);
        long time = data.getLong("time");
        long tid = data.getLong("tid");
        db.execSQL("DELETE FROM timer WHERE tid = "+ tid +" AND time = "+ time);

        ContentValues val = new ContentValues();
        val.put("time", time);
        val.put("tid", tid);
        return db.insert("timer",null, val);
    }

    public void delTimer(long tid, long id){
        SQLiteDatabase db = db(1);
        if(id > 0) {
            db.execSQL("DELETE FROM timer WHERE tid = " + tid + " AND id < " + id);
        }else{
            db.execSQL("DELETE FROM timer WHERE tid = " + tid);
        }
    }

    public void importTask(String sql) {
        SQLiteDatabase db = db(1);
        long id = getTaskLastId() + 1;
        long time = System.currentTimeMillis();
        String[] arr = sql.trim().split("[\r\n]+");

        for(int i = 0; i < arr.length; i++){
            if(i == 0){
                db.execSQL("INSERT INTO task VALUES("+ id +",1,"+ arr[i] +",0,'',"+ time +",0,0)");
            }else{
                db.execSQL("INSERT INTO flow (tid, cmd, obj, val, ctl, memo) VALUES("+ id +","+ arr[i] +")");
            }
        }
        db.execSQL("UPDATE flow SET val = REPLACE(val, '\\n', X'0A')");
    }

    public String exportTask(long id) {
        StringBuilder sql = new StringBuilder();
        SQLiteDatabase db = db(0);

        Cursor rs = db.rawQuery("SELECT name FROM task WHERE id = "+ id, null);
        try {
            if (rs.moveToNext()) {
                sql = new StringBuilder("'"+ slashes(rs.getString(0)) +"'");
            }
        } finally {
            rs.close();
        }

        if(sql.length() > 0){
            rs = db.rawQuery("SELECT cmd, obj, val, ctl, memo FROM flow WHERE tid = "+ id +" ORDER BY sort DESC, id ASC", null);
            try {
                String item;
                while (rs.moveToNext()) {
                    item = "'"+ rs.getString(0) +"','"+ slashes(rs.getString(1)) +"','"+ slashes(rs.getString(2)) +"','"+ rs.getString(3) +"','"+ slashes(rs.getString(4)) +"'";
                    sql.append("\n\n").append(item);
                }
            } finally {
                rs.close();
            }
        }
        return sql.toString();
    }

    private String slashes(String sql){
        sql = sql.replace("\"", "'");
        sql = sql.replace("'", "''");
        sql = sql.replace("\n", "\\n");
        return sql;
    }

    public void close(){
        if(dbr != null) {
            dbr.close();
            dbr = null;
        }
        if(dbw != null) {
            dbw.close();
            dbw = null;
        }
    }

    public SQLiteDatabase db(int type){
        SQLiteDatabase db;
        if(type == 0){
            if(dbr == null) dbr = getReadableDatabase();
            db = dbr;
        }else{
            if(dbw == null) dbw = getWritableDatabase();
            db = dbw;
        }
        return db;
    }
}
