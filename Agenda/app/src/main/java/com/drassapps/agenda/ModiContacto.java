package com.drassapps.agenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;


public class ModiContacto extends Activity {

    private EditText nombre, movil, direccion, mail;
    private String n_BD, m_BD, d_BD, ma_BD;
    private int id;
    private ImageView imagen;
    private Button guardar, cancelar;

    private View mLayout;

    public static final int PICK_IMAGE = 1;
    private Bitmap selectedImage;
    private Uri imageUri;

    private TextInputLayout ti_Nombre, ti_Direccion, ti_Movil, ti_Mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modi_contacto);

        // Recoge el contenedor padre de la vista
        mLayout = findViewById(R.id.lay_modicontacto);

        // Asignamos la imagen del contacto
        imagen = (ImageView) findViewById(R.id.image_foto);

        // Asigna los EditText
        nombre = (EditText) findViewById(R.id.modi_nombre);
        movil = (EditText) findViewById(R.id.modi_movil);
        direccion = (EditText) findViewById(R.id.modi_direccion);
        mail = (EditText) findViewById(R.id.modi_mail);

        // Asigna los TextInputLayot
        ti_Nombre = (TextInputLayout) findViewById(R.id.tinp_modi_nombre);
        ti_Direccion = (TextInputLayout) findViewById(R.id.tinp_modi_direccion);
        ti_Movil = (TextInputLayout) findViewById(R.id.tinp_modi_movil);
        ti_Mail = (TextInputLayout) findViewById(R.id.tinp_modi_correo);

        // Asigna los Buttons
        guardar = (Button) findViewById(R.id.modi_guardar);
        cancelar = (Button) findViewById(R.id.modi_cancelar);

        // Coge los textos pasados de la clase MainActivity
        Bundle extras= getIntent().getExtras();
        id = extras.getInt("ID");
        n_BD = extras.getString("Nombre");
        m_BD = extras.getString("Movil");
        d_BD = extras.getString("Direccion");
        ma_BD = extras.getString("Mail");

        // Establece el texto de los EditText y la iamgen
        nombre.setText(n_BD);
        movil.setText(m_BD);
        direccion.setText(d_BD);
        mail.setText(ma_BD);
        imagen.setImageBitmap(recogerImagen(n_BD+".png"));

        // Abrimos un intent a la galeria para que el usuario elija una imagen de la galeria
        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        // Devuelve a la vista padre los atributos cambiados para que los modifique en la BD
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devolverResultado(v,1);
            }
        });

        // Vuelve atras y da un mensaje de no se ha modificado
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devolverResultado(v,2);
            }
        });

        // Permite gestionar si los inputs del usuario son correctos
        mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                correoValido(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        movil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                movilValido(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        nombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nombreValido(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        direccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                direccionValida(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Permite gestionar si los inputs del usuario son correctos
    private boolean correoValido(String correo) {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            ti_Mail.setError(getString(R.string.mail_inv));
            return false;
        } else { ti_Mail.setError(null); }

        return true;
    }
    private boolean nombreValido(String nombre) {
        Pattern patron = Pattern.compile("^[a-zA-Z ]+$");
        if (!patron.matcher(nombre).matches() || nombre.length() > 30 || nombre.length() < 3) {
            ti_Nombre.setError(getString(R.string.nm_inv));
            return false;
        } else { ti_Nombre.setError(null); }

        return true;
    }
    private boolean movilValido(String telefono) {
        if (!Patterns.PHONE.matcher(telefono).matches() || telefono.length() != 9) {
            ti_Movil.setError(getString(R.string.movil_inv));
            return false;
        } else { ti_Movil.setError(null); }

        return true;
    }
    private boolean direccionValida(String nombre) {
        Pattern patron = Pattern.compile("^[a-zA-Z0-9 /ºª-]+$");
        if (!patron.matcher(nombre).matches() || nombre.length() > 30 || nombre.length() < 5) {
            ti_Direccion.setError(getString(R.string.direccion_inv));
            return false;
        } else { ti_Direccion.setError(null); }

        return true;
    }

    // Segun el boton pulsado de la vista
    public void devolverResultado(View v,int valor)
    {
        Intent i= new Intent();
        i.putExtra("ID",id);

        // Devuelve a la vista padre los atributos cambiados para que los modifique en la BD
        if (valor==1) {

            // Si los campos son correctos
            if(correoValido(mail.getText().toString()) && nombreValido(nombre.getText().toString())
                    && movilValido(movil.getText().toString())
                    && direccionValida(direccion.getText().toString())){

                setResult(RESULT_OK,i);

                i.putExtra("Nombre", nombre.getText().toString());
                i.putExtra("Direccion", direccion.getText().toString());
                i.putExtra("Movil", movil.getText().toString());
                i.putExtra("Mail", mail.getText().toString());

                // Borramos la archivo de la imagen que tenemos guardada, y creamos una nueva con
                // el nuevo nombre del contacto

                // Nombre viejo
                File fichero = new File(n_BD+".png");
                fichero.delete();

                // Recoge la imagen en formato bitmap para posterior mente guardarla en la SD
                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File sdCardDirectory = Environment.getExternalStorageDirectory();
                File image = new File(sdCardDirectory, nombre.getText().toString()+".png");

                // Codifica el archivo como una imagen
                FileOutputStream outStream;
                try {

                    outStream = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                    outStream.flush();
                    outStream.close();
                }
                catch (FileNotFoundException e) { e.printStackTrace(); }
                catch (IOException e) { e.printStackTrace(); }


                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                finish();

                // Algun campo no tiene el input correcto
            }else { setSnackBar(mLayout,getString(R.string.datos_inv)); }
        }
        else { // Vuelve atras y da un mensaje de no se ha modificado
            setResult(90,i);

            overridePendingTransition(R.anim.left_in, R.anim.left_out);
            finish();
        }
    }

    // Fuerza la creacion de un SnackBar, personificado
    public static void setSnackBar(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_SHORT);
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private Bitmap recogerImagen(String c){
        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), c);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return  bitmap;
    }

    // Recogemos la imagen elegida por el usuario
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Si ha elegido una imagen
        if (requestCode == PICK_IMAGE) {
            try{
                // Si tenemos datos
                if (data != null){

                    // Obtenemos una URI de la ubicación de la imagen
                    imageUri = data.getData();

                    // Abrimos la URI a través de un InputStram
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                    // Convertimos ese inputstream a un BitMap
                    selectedImage = BitmapFactory.decodeStream(imageStream);

                    // La asignamos a la ImagevIEW
                    imagen.setImageBitmap(selectedImage);

                    // Si no hay data significa que el usuario no ha elegido ninguna imagen
                }else{
                    setSnackBar(mLayout,getString(R.string.ErrorPick));
                }

            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
