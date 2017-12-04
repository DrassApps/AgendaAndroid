package com.drassapps.agenda;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

public class Adaptador extends CursorAdapter{

    private LayoutInflater inflador; // Crea Layouts a partir del XML
    TextView nombre, movil;
    ImageView imagen;

    public Adaptador(Context contexto, Cursor c) { super(contexto, c, false); }

    // Infla la view de la lista
    // d_list contiene el diseño de la lista que se va a mostrar en el ListView del MainAct.
    @Override
    public View newView(Context contexto, Cursor c, ViewGroup padre) {

        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vista = inflador.inflate(R.layout.d_list, padre, false);
        return vista;
    }

    // Rellana la lista con los datos del cursor que le hemos pasado
    @Override
    public void bindView(View vista, Context contexto, Cursor c) {

        // Asignamos los elemetnos del Layout
        nombre = (TextView) vista.findViewById(R.id.d_list_nombre);
        movil = (TextView) vista.findViewById(R.id.d_list_movil);
        imagen = (ImageView) vista.findViewById(R.id.image_contact);


        // Asignamos los valores
        nombre.setText(c.getString(c.getColumnIndex("nombre")));
        movil.setText(c.getString(c.getColumnIndex("movil")));
        imagen.setImageBitmap(recogerImagen(c.getString(c.getColumnIndex("nombre"))+".png"));
    }

    // Dado un string de ruta devuelve un bitmap con la imagen
    private Bitmap recogerImagen(String c){
        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), c);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return  bitmap;
    }


}
