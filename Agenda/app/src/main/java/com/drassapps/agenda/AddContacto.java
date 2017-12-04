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
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class AddContacto extends Activity {

    private EditText nombre, direccion, movil, mail;
    private Button guardar, cancelar;
    private ImageView image_foto;
    private View mLayout;

    public static final int PICK_IMAGE = 1;

    private Bitmap selectedImage;
    private Uri imageUri;

    private TextInputLayout ti_Nombre, ti_Direccion, ti_Movil, ti_Mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contacto);

        // Recoge el contenedor padre de la vista
        mLayout = findViewById(R.id.layContacto);

        // Asigna los EditText
        nombre = (EditText) findViewById(R.id.text_nombre);
        direccion = (EditText) findViewById(R.id.text_direccion);
        movil = (EditText) findViewById(R.id.text_movil);
        mail = (EditText) findViewById(R.id.text_mail);

        image_foto = (ImageView) findViewById(R.id.image_foto);

        // Asigna los TextInputLayot
        ti_Nombre = (TextInputLayout) findViewById(R.id.tinp_nombre);
        ti_Direccion = (TextInputLayout) findViewById(R.id.tinp_direccion);
        ti_Movil = (TextInputLayout) findViewById(R.id.tinp_movil);
        ti_Mail = (TextInputLayout) findViewById(R.id.tinp_correo);

        // Asigna los Buttons
        guardar = (Button) findViewById(R.id.guardar);
        cancelar = (Button) findViewById(R.id.cancelar);

        // Abrimos un intent a la galeria para que el usuario elija una imagen de la galeria
        image_foto.setOnClickListener(new View.OnClickListener() {
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
    public void devolverResultado (View v, int v_C)
    {
        Intent i= new Intent();

        // Devuelve a la vista padre los atributos cambiados para que los modifique en la BD
        if (v_C==1) {

            // Si los campos son correctos
            if(correoValido(mail.getText().toString()) && nombreValido(nombre.getText().toString())
                    && movilValido(movil.getText().toString())
                    && direccionValida(direccion.getText().toString())){

                setResult(RESULT_OK,i);

                // Convierte la primera letra del nombre a mayuscula
                String nombre_mays = nombre.getText().toString();
                nombre_mays = Character.toUpperCase(nombre_mays.charAt(0))
                        + nombre_mays.substring(1,nombre_mays.length());

                // Recogo los datos para pasarselo a la calse MainActivity
                i.putExtra("Nombre", nombre_mays);
                i.putExtra("Direccion", direccion.getText().toString());
                i.putExtra("Movil", movil.getText().toString());
                i.putExtra("Mail", mail.getText().toString());

                // Recoge la imagen en formato bitmap para posterior mente guardarla en la SD
                BitmapDrawable drawable = (BitmapDrawable) image_foto.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File sdCardDirectory = Environment.getExternalStorageDirectory();
                File image = new File(sdCardDirectory, nombre_mays+".png");

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


                // Crea una animacion
                overridePendingTransition(R.anim.left_in, R.anim.left_out);

                finish();

                // Algun campo no tiene el input correcto
            }else { setSnackBar(mLayout,getString(R.string.datos_inv)); }
        }
        else{ // Vuelve atras y da un mensaje de no se ha modificado
            setResult(RESULT_CANCELED,i);

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
                    image_foto.setImageBitmap(selectedImage);

                // Si no hay data significa que el usuario no ha elegido ninguna imagen
                }else{
                    setSnackBar(mLayout,getString(R.string.ErrorPick));
                }

            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
