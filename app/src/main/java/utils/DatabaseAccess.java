package utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    Cursor c = null;
    Cursor c1 = null;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context){
        if(instance == null){
            instance=new DatabaseAccess(context);
        }
        return instance;
    }
    public void open(){
        this.db = openHelper.getWritableDatabase();
    }

    public void close(){
        if(db != null){
            this.db.close();
        }
    }

    public List<Monument> getName(double longitude, double latitude, float zoom){
        float markerOnZoom = Math.abs(zoom - 21); // controllo quanto lo zoom è distante dallo zoom più "vicino"
        float distance = (markerOnZoom/14)*2;
        c = db.rawQuery("select name, longitude, latitude from monuments where longitude > "+ (longitude-distance)+" and longitude < "+ (longitude+distance) +" and Latitude > "+(latitude-distance) +" and Latitude < "+(latitude+distance),new String[]{});
        List<Monument> buffer = new ArrayList<>();
        while(c.moveToNext()){
            String name = c.getString(0);
            double longitudeplace = c.getDouble(1);
            double latitudeplace = c.getDouble(2);
            buffer.add(new Monument(name,latitudeplace,longitudeplace));
        }
        return buffer;
    }
}
