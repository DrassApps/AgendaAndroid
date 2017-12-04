package com.drassapps.agenda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.CursorAdapter;


public class BDAgenda extends SQLiteOpenHelper {

    private static final int VERSION_BASEDATOS = 4;
    private static final String NOMBRE_BASEDATOS = "BDAgenda.db";

    // Creacion de tabla para la BDen formato SQL
    private static final String TABLA_NOTAS =
            "CREATE TABLE contactos " +
                    "(_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    " nombre VARCHAR(100), direccion VARCHAR(100), movil VARCHAR(100)," +
                    " mail VARCHAR(100));";


    // Constructor de la clase
    public BDAgenda(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_NOTAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contactos");
        onCreate(db);
    }

    // Inserta un contactos a la BD
    public void insertarContacto(String nombre, String direccion, String movil, String mail) {

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            // Creamos el registro a insertar
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            valores.put("direccion", direccion);
            valores.put("movil", movil);
            valores.put("mail", mail);

            //insertamos el registro en la Base de Datos
            db.insert("contactos", null, valores);
        }

        db.close();
    }

    // Modifica un contacto del a BD
    public void modificarContacto(int id, String nombre, String direccion,String movil,String mail){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("direccion", direccion);
        valores.put("movil", movil);
        valores.put("mail", mail);

        db.update("contactos", valores, "_id=" + id, null);

        db.close();
    }

    // Borra un contacto del a BD dado un id
    public void borraContacto(int id) {

        SQLiteDatabase db = getWritableDatabase();

        db.delete("contactos", "_id=" + id, null);

        db.close();
    }



    // Da unos valores a la clase de Contacto
    public Contacto recuperarContacto(int id) {

        SQLiteDatabase db = getReadableDatabase();

        String[] valores_recuperar = {"_id", "nombre", "direccion", "movil", "mail"};

        Cursor c = db.query("contactos", valores_recuperar, "_id=" + id, null,null,null,null,null);

        if(c != null) {
            c.moveToFirst();
        }

        Contacto contacto = new Contacto(c.getInt(0), c.getString(1),
                c.getString(2), c.getString(3), c.getString(4));

        db.close();
        c.close();

        return contacto;
    }

    // Devuelve datos de un Contacto dado un nombre, los devuelve de forma orednada por nombre
    public Cursor busquedaContacto(String nombre) {

        if(nombre.length() > 0){
            SQLiteDatabase db = getReadableDatabase();

            String[] valores_recuperar = {"_id", "nombre", "direccion", "movil", "mail"};

            String[] args = new String[] {nombre};

            Cursor c = db.query("contactos", valores_recuperar, "nombre=?", args, null, null,
                    "nombre ASC",null);
            return c;

        }
        else {
            SQLiteDatabase db = getWritableDatabase();

            String[] valores_recuperar = {"_id","movil","nombre"};

            // Ordena al recuperarlos
            Cursor c= db.query("contactos",valores_recuperar,null,null,null,null,"nombre ASC",null);

            return c;
        }
    }

    // Devuelve un cursos con todos los atributos elegidos de la BD para ponerlos en la Lista
    public Cursor recuperarCursosdeContactos() {

        SQLiteDatabase db = getWritableDatabase();

        String[] valores_recuperar = {"_id","movil","nombre"};

        // Ordena al recuperarlos
        Cursor c = db.query("contactos", valores_recuperar,null,null,null,null,"nombre ASC",null);

        return c;
    }

    // Devuelve un cursos con todos los atributos elegidos de la BD para ponerlos en la Lista
    public Cursor recuperarCursosdeContactosPorAntiguedad() {

        SQLiteDatabase db = getWritableDatabase();

        String[] valores_recuperar = {"_id","movil","nombre"};

        // Ordena al recuperarlos por id
        Cursor c = db.query("contactos", valores_recuperar,null,null,null,null,"_id ASC",null);

        return c;
    }



    // Devuelve el numero de filas de la tabla
    public int numerodeFilas(){
        int dato= (int) DatabaseUtils.queryNumEntries(this.getWritableDatabase(), "contactos");
        return dato;
    }

    // Guarda en un array de IDs todos los ID de la tabla contactos
    public int [] recuperaIds(){
        int [] datosId;
        int i;

        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"_id"};

        // Cuando recupera los IDS lo tiene que hacer ordenado por el nombre como la lista
        Cursor cursor = db.query("contactos",valores_recuperar,null,null,null,null,"nombre ASC",null);

        if (cursor.getCount()>0){
            datosId= new int[cursor.getCount()];
            i=0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                datosId[i] = cursor.getInt(0);
                i++;
                cursor.moveToNext();
            }
        }
        else datosId = new int [0];
        cursor.close();
        return datosId;
    }

}
