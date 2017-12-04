package com.drassapps.agenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class VerContacto extends Activity {

    private TextView nombre, movil, direccion, mail;    // TextView del para datos
    private ImageButton llamar;                         // ImageButton asociado a llamar al contacto
    protected ImageView imageView;                      // ImageView para el contacto
    private String n_BD, m_BD, d_BD, ma_BD;             // Strings que recogen los datos pasados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver_contacto);

        // Asigna los TextView
        nombre = (TextView) findViewById(R.id.vc_nombre);
        movil = (TextView) findViewById(R.id.vc_movil);
        direccion = (TextView) findViewById(R.id.vc_direccion);
        mail = (TextView) findViewById(R.id.vc_mail);

        // Asigna la ImageView e ImageButton
        imageView = (ImageView) findViewById(R.id.vc_image_foto);
        llamar = (ImageButton) findViewById(R.id.vc_llamar);

        // Recoge los datos pasados de la actividad padre
        Bundle extras= getIntent().getExtras();
        n_BD = extras.getString("Nombre");
        m_BD = extras.getString("Movil");
        d_BD = extras.getString("Direccion");
        ma_BD = extras.getString("Mail");

        // Establece texto para los TextView
        nombre.setText(n_BD);
        movil.setText(m_BD);
        direccion.setText(d_BD);
        mail.setText(ma_BD);

        // Establece la imagen
        imageView.setImageBitmap(recogerImagen(n_BD+".png"));

        llamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:(+34)" + m_BD));
                startActivity(i);
            }
        });

    }

    // Dado un string de ruta devuelve un bitmap con la imagen
    private Bitmap recogerImagen(String c){
        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), c);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return  bitmap;
    }

}
