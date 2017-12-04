package com.drassapps.agenda;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends ListActivity {

    // Nos permite gestionar los eventos de los permisos de usuario
    private static final int MY_WRITE_EXTERNAL_STORAGE = 0;
    public final static int C_Aceptado=1;
    public final static int C_Modi=3;
    public final static int RESULT_BORRAR=14;
    public final static int RESULT_MODI_CANCELEDE=90;

    boolean sdDisponible =	false;          // Bool que indica si la SD esta disponible
    boolean sdAccesoEscritura =	false;      // Bool que indica si la SD tiene acceso a escritura

    private DrawerLayout drawerLayout;      // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;          // Creacion de var NavigationView

    String textoLecturaFin = "";            // String asociada al contenido de la lectura de la SD

    private LinearLayout btt_linear;        // LinearLayout asociado a la vista BottomSheet
    private BottomSheetBehavior bSB;        // Creacion de var BottomSheetBehavior

    private View mLayout;                   // Vista principal
    private ImageView add_contacto;         // ImagenView para añadir un contacto a la Agenda
    private ImageView bt_menu_nav,menu_nav; // ImageView que gestionan abrir y cerrar Nav
    private LinearLayout dummy_line;        // Dumy linear que permite que no se focuse otro campo
    private LinearLayout main;              // MainLayout
    private EditText text_busqueda;         // Campo asociado a la Toolbar que nos permite buscar
    private TextInputLayout tp_nombre;      // TextInpt del materialDesing

    // Campos asociados al BottomSheet
    private ImageView btsheet_low, btsheet_image_foto, btsheet_call;
    private TextView btsheet_nombre, btsheet_movil;

    private BDAgenda bdAgenda;          // Referencia a la clase agenda, para gestionar sus metodos
    private Adaptador adaptador;        // Referencia a la clase adaptadar, gestiona sus metodos
    private int numFilas;               // Entero que contendrá el numero de filas de la BD
    private int [] ids;                 // Array que contiene los ids de los contactos
    private int iDAct = 0;              // Entero que referencia al id marcado por el usuario


    // Permite gestionar que se mantengan los datos cuando se cambia la orientacion de pantalla
    private SharedPreferences prefs;
    public static final String MyPREFERENCES = "MyPrefs";
    public String pref_movil = "movil";
    public String pref_nombre = "nombre";
    public String pNombre, pMovil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asignamos la vista y el linear global
        mLayout = findViewById(R.id.lay_main);
        main = (LinearLayout) findViewById(R.id.lay_main1);

        prefs = getSharedPreferences(MyPREFERENCES,MODE_PRIVATE);
        pNombre = prefs.getString("nombre", "");
        pMovil = prefs.getString("movil", "");

        // Verificamos los permisos del usuarios
        verificarPermisos();

        // Asignamos las ImageView de la vista
        add_contacto = (ImageView) findViewById(R.id.add);
        menu_nav = (ImageView) findViewById(R.id.menu_nav);
        bt_menu_nav= (ImageView) findViewById(R.id.bt_menu_nav);

        // Asignamos los EditTet para la busqueda de un contacto
        text_busqueda = (EditText) findViewById(R.id.busqueda_contato);
        tp_nombre = (TextInputLayout) findViewById(R.id.tinp_nombre_buscar);

        // Dumy line para que cuando se abra la aplicación el EditTet no salga focuseado
        dummy_line = (LinearLayout) findViewById(R.id.dummy_line);

        // Asignamos la toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        // Controlamos los botonos de la toolbar
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    // Si pulsamos buscar, nos buscará el contacto por el nombre
                    case R.id.Busqueda:
                        buscarContacto();
                        return true;

                    // Abre el dialogo de exportar contactos
                    case R.id.Exportar:
                        exportarContatos();
                        return true;

                    // Abre el dialogo de importar contactos
                    case R.id.Importar:
                        importarContatos();
                        return true;
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);

        // Configura el bottomSheet
        configuraBottonSheet();

        // Configuración del NavigationView
        bundle = (NavigationView) findViewById(R.id.navview);
        drawerLayout  = (DrawerLayout)findViewById(R.id.drawer_layout);

        // Añadimos el NavigationView a la vista
        if (bundle != null) { setupDrawerContent(bundle); }

        // El boton del lay abre el nav
        menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        // El botón dentro del nav, cierra el nav
        bt_menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.START);
                dummy_line.requestFocus();
            }
        });


        // Botón que nos permite añadir un contacto
        add_contacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { add_contacto(v); }
        });

        // Nos permite gestionar la aparencia de los colores del TextInput segun los valores
        // introducidos por el usuario
        text_busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nombreValido(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Dumy line que nos permite que cuando se abra la aplicación el EditTet no salga focuseado
        text_busqueda.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    dummy_line.isFocused();
                    return true;
                }
                return false;
            }
        });

        // Asigna el constructor de la clase a la variable bdAgenda para gestionar la BD.
        bdAgenda = new BDAgenda(getApplicationContext());

        // Obtiene los valores correspondientes de la BD y se los pasa al adaptador para
        // que construya el ListView
        rellenaLista();

        // Necesario para que se abra el menuContextual cuando hacemos loong Click
        // sobre un elemento de la lista
        registerForContextMenu(getListView());

        // Cuando pulsamos sobre el linear principal nos cierra el BottomSheet
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSB.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }


    // Pasamos a la vista de concactos para añadir un contacto
    public void add_contacto(View vista) {
        // Creamos un intent a la clase AddContacto
        Intent i = new Intent(this,AddContacto.class);

        // Creamos una animacion para el paso a la siguiente vista
        overridePendingTransition(R.anim.left_in, R.anim.left_out);

        // Esperamos resultado de la clas AddContacto
        startActivityForResult(i,C_Aceptado);
    }

    // Dadod un id del List accede a la Vista ModificarContacto
    public void modiContacto(View vista){
        Contacto contacto;
        Intent i = new Intent(this,ModiContacto.class);

        // Obtiene los valores del un determinado Contacto de la BD para pasarselos a la vista
        contacto= bdAgenda.recuperarContacto(iDAct);

        // Pasamos los valores a la vista
        i.putExtra("ID",iDAct);
        i.putExtra("Nombre", contacto.getNombre());
        i.putExtra("Movil", contacto.getMovil());
        i.putExtra("Direccion", contacto.getDireccion());
        i.putExtra("Mail", contacto.getMail());

        // Transicion entre animaciones
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
        startActivityForResult(i,C_Modi);
    }


    // Al pulsar el contacto accedemos a la vista de VerContacto para verlo
    public void verContacto(View vista) {
        Contacto contacto;
        Intent i = new Intent(this,VerContacto.class);

        i.putExtra("ID",iDAct);

        contacto= bdAgenda.recuperarContacto(iDAct);

        i.putExtra("Nombre", contacto.getNombre());
        i.putExtra("Movil", contacto.getMovil());
        i.putExtra("Direccion", contacto.getDireccion());
        i.putExtra("Mail", contacto.getMail());

        overridePendingTransition(R.anim.left_in, R.anim.left_out);
        startActivity(i);
    }


    // Devuelve el contacto que se ha buscado
    public void buscarContacto(){

        // Recoge el string del EditText
        String n = text_busqueda.getText().toString();

        // Crea una nuevo adaptador pasandole solo los datos del contacto en cuestion
        // para a continuacion mostrar solo el usuario buscado
        adaptador = new Adaptador(this, bdAgenda.busquedaContacto(n));

        Cursor c = bdAgenda.busquedaContacto(n);
        String num;

        // Reasignamos los ids despues del buscar de tal forma nos mostrará los datos correctos
        // para el contacto elegido.
        if( c != null && c.moveToFirst()) {
            num = c.getString(c.getColumnIndex("_id"));
            c.close();
            ids[0] = Integer.parseInt(num);
        }

        // Establecemos el nuevo adaptador y le indicamos que los datos se han cambiado
        setListAdapter(adaptador);
        adaptador.notifyDataSetChanged();
    }

    // Da valores a la Lista segun lo que hay en la BD
    public void rellenaLista(){
        // Obtiene el numero de fila de la BD
        numFilas=bdAgenda.numerodeFilas();

        // Para cada numero de fila recupera los ids
        if (numFilas> 0) {ids= bdAgenda.recuperaIds();}

        // Obtiene los contactos de la BD los pasa al adaptador par que los cree
        adaptador = new Adaptador(this, bdAgenda.recuperarCursosdeContactos());
        // Establece el adaptador
        setListAdapter(adaptador);

    }

    // Si pulsamos el un item de la Lista, aparece la vista de VerContacto con el contenido
    // del id seleccionado, asi mismo podremos llamar desde esta vista
    @Override
    protected void onListItemClick(ListView lv, View view, int posicion, long id) {
        iDAct= ids[posicion];
        muestraDatosenBtSh();
    }

    // Obtenemos el contenido de la vista AddContacto
    protected void onActivityResult(int resul, int codigo,Intent data)
    {
        if (codigo==RESULT_OK)
        {
            // Si me devuelven un 1, quiere decir que han añadido un contacto, por lo que
            // guardamos los datos y volvemos a mostrar la lista
            if (resul==C_Aceptado)
            {
                String nombre = data.getExtras().getString("Nombre");
                String direccion = data.getExtras().getString("Direccion");
                String movil = data.getExtras().getString("Movil");
                String mail = data.getExtras().getString("Mail");

                setSnackBar(mLayout,nombre + " " + getString(R.string.add_agenda));

                bdAgenda.insertarContacto(nombre,direccion,movil,mail);
                rellenaLista();
            }

            // Si el resultado es correcto pero no devuelve un 1 es una modficiacion
            else
            {
                String nombre = data.getExtras().getString("Nombre");
                String direccion = data.getExtras().getString("Direccion");
                String movil = data.getExtras().getString("Movil");
                String mail = data.getExtras().getString("Mail");

                setSnackBar(mLayout,nombre + " " + getString(R.string.modi_agenda));

                int id= data.getExtras().getInt("ID");
                bdAgenda.modificarContacto(id,nombre,direccion,movil,mail);
                rellenaLista();
            }
        }

        // Si  el codigo es borrar, borramos el contacto y mostramos un mensaje al usuario
        else if(codigo==RESULT_BORRAR)
        {
            int id= data.getExtras().getInt("ID");
            bdAgenda.borraContacto(id);
            setSnackBar(mLayout, getString(R.string.del_agenda));
            rellenaLista();
        }

        // Si el usuario cancela las operaciones, indicamos que no se ha añadido ningun contacto
        else if(codigo==RESULT_CANCELED)
        { setSnackBar(mLayout,getString(R.string.err_agenda)); }

        // Si el usuario cancela las operaciones, indicamos que no se ha modificado ningun valor
        else if(codigo==RESULT_MODI_CANCELEDE)
        { setSnackBar(mLayout,getString(R.string.modiN_agenda)); }
    }

    // Método donde definimos el menú contextual cuando se despliega
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Creaamos e inflamos el menu
        MenuInflater inflater = getMenuInflater();
        if(v.getId() == getListView().getId()){
            menu.setHeaderTitle(R.string.eligeOpcion);
        }
        inflater.inflate(R.menu.menu_cont_lista, menu);
    }

    // Permite gestionar los eventos del menu contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()) {

            // Si pulsa sobre ver se abrirar la vista de ver contacto
            case R.id.Ver:
                iDAct= ids[info.position];
                verContacto(getListView());
                return true;

            // Si pulsa sobre eliminar se eliminara el contacto y mostrara un mensaje
            case R.id.Eliminar:
                iDAct= ids[info.position];
                bdAgenda.borraContacto(iDAct);
                setSnackBar(mLayout,getString(R.string.c_eliminado));
                rellenaLista();
                return true;

            // Si pulsa sobre nuevo, se abrirar la vista de crear un nuevo contacto
            case R.id.Nuevo:
                iDAct= ids[info.position];
                add_contacto(getListView());
                rellenaLista();
                return true;

            // Si pulsa sobre modificar se abrirá la vista de modificar un contacto
            case R.id.Modificar:
                iDAct= ids[info.position];
                modiContacto(getListView());
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    // Asigna los elementos del Linear a una variables para poder gestionarlas
    private void configuraBottonSheet(){

        btt_linear = (LinearLayout)findViewById(R.id.btt_sh);
        bSB = BottomSheetBehavior.from(btt_linear);

        btsheet_low = (ImageView) findViewById(R.id.btsheep_low);
        btsheet_image_foto = (ImageView) findViewById(R.id.btsheep_image_foto);
        btsheet_call = (ImageView) findViewById(R.id.btsheep_call);
        btsheet_nombre = (TextView) findViewById(R.id.btsheep_nombre);
        btsheet_movil = (TextView) findViewById(R.id.btsheep_movil);

        if(pNombre.length()>4 && pMovil.length()>4){
            btsheet_nombre.setText(pNombre);
            btsheet_movil.setText(pMovil);
            btsheet_image_foto.setImageBitmap(recogerImagen(pNombre+".png"));
        }
    }



    // Abre el Lay del BotomSheet con los datos del usuario
    private void muestraDatosenBtSh(){

        verificarPermisos();

        final Contacto contacto;

        bSB.setState(BottomSheetBehavior.STATE_EXPANDED);

        contacto= bdAgenda.recuperarContacto(iDAct);

        btsheet_nombre.setText(contacto.getNombre());
        btsheet_movil.setText(contacto.getMovil());
        btsheet_image_foto.setImageBitmap(recogerImagen(contacto.getNombre()+".png"));

        bSB.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Cuando esta activo el bSB ocultamos el boton de añadir un nuevo contacto ya
                // que si pulsabamos donde esta en la vista se abriria, aunque este detras del BSB
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    add_contacto.setVisibility(View.INVISIBLE);
                }

                // Si esta cerrado lo volvemos a mostrar
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    add_contacto.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}

        });

        // Cierra el btsheep cuando pulsamos el boton
        btsheet_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSB.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // Abre el telefono para llamar al contacto
        btsheet_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:(+34)" + contacto.getMovil()));
                startActivity(i);
            }
        });

    }

    // Comprueba si tenemos montada al SD
    private void comprobarSD(){
        String estado	=	Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED)) {
            sdDisponible =	true;
            sdAccesoEscritura =	true;
        }
        else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            sdDisponible =	true;
            sdAccesoEscritura =	false;
        }
        else {
            sdDisponible =	false;
            sdAccesoEscritura =	false;
        }
    }

    // Guarda los contactos en la SD.
    private void exportarContatos() {

        // Comprobamos si tenemos una SD montada.
        comprobarSD();

        // Mostramos un dialogo al usuario con dos opciones TXT y JSON
        if (sdAccesoEscritura) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage(R.string.dialogExport)
                    .setTitle(R.string.aviso)
                    .setNegativeButton(R.string.txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Si elige Texto
                            crearFileTxt();
                        }
                    })
                    .setPositiveButton(R.string.json, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Si elige JSON
                            crearFileJson();
                        }
                    });

            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

        } else { setSnackBar(mLayout,getString(R.string.errorSD)); }
    }

    // Importa los contactos desde la SD.
    private void importarContatos() {

        // Comprobamos si tenemos una SD montada.
        comprobarSD();

        // Mostramos un dialogo al usuario con dos opciones TXT y JSON
        if (sdAccesoEscritura) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage(R.string.dialogExport)
                    .setTitle(R.string.aviso)
                    .setNegativeButton(R.string.txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Si eiige texto
                            importaFileTxt();
                        }
                    })
                    .setPositiveButton(R.string.json, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Si elige JSON
                            importaFileJson();
                        }
                    });

            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

        } else { setSnackBar(mLayout,getString(R.string.errorSD)); }
    }

    // Crea el fichero Contactos.txt con los datos obtenidos de la lista
    public void crearFileTxt(){

        // Establecemos la ruta donde guardar el archivo .txt
        File ruta = Environment.getExternalStorageDirectory();
        // Creamos el archivo
        File file = new File(ruta.getAbsolutePath(), "Contactos.txt");
        // String que contiene toda la informacion
        String textoFin = "";
        // Necesario para acceder a los atributos del contacto
        Contacto contacto;

        // Si no hay usuarios le mostramos un error
        if(getListView().getCount() == 0){ setSnackBar(mLayout,"No tienes datos para exportar");}
        else{
            // Cargamos los datos
            for (int i=0; i<getListView().getCount(); i++){
                contacto= bdAgenda.recuperarContacto(ids[i]);
                String aux = "nombre:" + contacto.getNombre() + ", " +
                        "movil:" + contacto.getMovil() + ", " +
                        "direcion:" + contacto.getDireccion() + ", " +
                        "mail:" + contacto.getMail() + "\n";
                textoFin += aux;
            }

            Log.i("Texto",""+textoFin);

            // Abrimos el archivo y escribimos los datos cargados previamente
            try {
                OutputStreamWriter f_out = new OutputStreamWriter(new FileOutputStream(file));
                f_out.write(textoFin);
                f_out.close();

                setSnackBar(mLayout,getString(R.string.crearTXTSC));

            } catch (Exception e) {setSnackBar(mLayout,getString(R.string.crearTXTFile));}
        }

    }

    // Crea el fichero Contatos.json con los datos obtenidos de la lista
    public void crearFileJson(){

        // Establecemos la ruta donde guardar el archivo .JSON
        File ruta = Environment.getExternalStorageDirectory();
        // Creamos el archivo
        File file = new File(ruta.getAbsolutePath(), "Contactos.json");
        // Creamos un JSArray
        JSONArray contactos = new JSONArray();
        // Necesario para acceder a los atributos del contacto
        Contacto contacto;

        try{
            // Para cada registro de la lista, añadimos sus valores en el objeto Json
            // y lo añadimos al array previamente creado.
            for (int i=0; i < getListView().getCount(); i++){
                JSONObject root = new JSONObject();
                contacto= bdAgenda.recuperarContacto(ids[i]);
                root.put("nombre",contacto.getNombre());
                root.put("movil",contacto.getMovil());
                root.put("direccion",contacto.getDireccion());
                root.put("mail",contacto.getMail());
                contactos.put(root);
            }

        }catch (Exception e){e.printStackTrace();}


        // Creamos un nuevo objeto JSON que nos servira para contener los distons arrays de dato
        // siendo cada array un contacto, teniendo al final un formato tipo:
        /*
            {"Contactos":
              [
                {
                    "nombre":"Andres",
                    "telefono":"123456789",
                    "xxx":"xx",...
                },
                {   "nombre":"xx",
                    "..."
                }
               ]
             }
        */

        JSONObject contacto_final = new JSONObject();
        try{
            contacto_final.put("Contactos",contactos);
        }catch (Exception e){e.printStackTrace();}


        // Abrimos el archvio como y añadimos la cadena en formato JSON
        try {

            OutputStreamWriter f_out = new OutputStreamWriter(new FileOutputStream(file));
            f_out.write(contacto_final.toString());
            f_out.close();
            setSnackBar(mLayout,getString(R.string.crearJSONSC));

        } catch (Exception e) {setSnackBar(mLayout,getString(R.string.crearJSONFile));}
    }

    // Recupera los contactos del fichero Contaxtos.txt
    public void importaFileTxt(){

        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), "Contactos.txt");

        // ArrayList para obtener los registros del File y añadirlos a la BD
        ArrayList<String> nombreFromFile =  new ArrayList<>();
        ArrayList<String> movilFromFile =  new ArrayList<>();
        ArrayList<String> direccionFromFile =  new ArrayList<>();
        ArrayList<String> mailFromFile =  new ArrayList<>();

        // Si no encuentra el archivo
        if (!file.exists()) {
            setSnackBar(mLayout, getString(R.string.importarTXT));
        } else {
            try {

                // Borramos los contactos actuales
                for (int i=0; i < getListView().getCount(); i++) {
                    bdAgenda.borraContacto(ids[i]);
                }

                // Clase para leer lineas desde un archivo en Java
                BufferedReader f_in = new BufferedReader(new
                        InputStreamReader(new FileInputStream(file)));

                // Mientras lea una linea del archivo
                while ((textoLecturaFin = f_in.readLine()) != null) {
                    Log.i("Inf"," "+ textoLecturaFin);
                    // Sep[0] contiene nombres
                    // Sep[1] contiene movil
                    // Sep[2] contiene direccion
                    // Sep[3] contiene mail

                    String[] sep = textoLecturaFin.split(", ");

                    // Nombre
                    String[] nm = sep[0].split(":");
                    nombreFromFile.add(nm[1]);

                    // Movil
                    String[] mv = sep[1].split(":");
                    movilFromFile.add(mv[1]);

                    // Direccion
                    String[] dr = sep[2].split(":");
                    direccionFromFile.add(dr[1]);

                    // Mail
                    String[] ml = sep[3].split(":");
                    mailFromFile.add(ml[1]);
                }

                // Insertamo los nuevos campos
                for (int j=0; j<nombreFromFile.size() ;j++){
                   bdAgenda.insertarContacto(nombreFromFile.get(j),direccionFromFile.get(j),
                            movilFromFile.get(j),mailFromFile.get(j));
                }

                f_in.close(); // Cerramos el file
                rellenaLista(); // Recargamos los datos para ver la lista
                setSnackBar(mLayout,getString(R.string.importarTXTSC));

            } catch (Exception e) {e.printStackTrace();}
        }
    }

    // Recupera los contactos del fichero Contaxtos.json
    public void importaFileJson(){

        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), "Contactos.json");

        // ArrayList para obtener los registros del File y añadirlos a la BD
        ArrayList<String> nombreFromFile =  new ArrayList<>();
        ArrayList<String> movilFromFile =  new ArrayList<>();
        ArrayList<String> direccionFromFile =  new ArrayList<>();
        ArrayList<String> mailFromFile =  new ArrayList<>();

        // Si no encuentra el archivo
        if (!file.exists()) {
            setSnackBar(mLayout, getString(R.string.importarJSON));

        } else {
            try {

                // Borramos los contactos actuales
                for (int i=0; i < getListView().getCount(); i++) {
                    bdAgenda.borraContacto(ids[i]);
                }

                // Clase para leer lineas desde un archivo en Java
                BufferedReader f_in = new BufferedReader(new
                        InputStreamReader(new FileInputStream(file)));

                // Pasamos el contenido del archvio a un string
                textoLecturaFin = f_in.readLine();

                // Parseamos el contenido a JSON
                JSONObject root = new JSONObject(textoLecturaFin);
                JSONArray jsonArray = root.getJSONArray("Contactos");

                for(int i=0; i<jsonArray.length(); i++) {

                    // Obtenemos el objeto contacto y los diferentes campos
                    JSONObject json_data = jsonArray.getJSONObject(i);

                    nombreFromFile.add(json_data.getString("nombre"));
                    movilFromFile.add(json_data.getString("movil"));
                    direccionFromFile.add(json_data.getString("direccion"));
                    mailFromFile.add(json_data.getString("mail"));

                }

                // Insertamo los nuevos campos
                for (int j=0; j<nombreFromFile.size() ;j++){
                    bdAgenda.insertarContacto(nombreFromFile.get(j),direccionFromFile.get(j),
                            movilFromFile.get(j),mailFromFile.get(j));
                }

                f_in.close(); // Cerramos el archivo
                rellenaLista(); // Cargamos de nuevo la vista con los datos importados
                setSnackBar(mLayout,getString(R.string.importarJSONSC));

            } catch (Exception e) {e.printStackTrace();}
        }
    }

    // Fuerza la creacion de un SnackBar, personificado
    public void setSnackBar(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_LONG);
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void setSnackBar2(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_LONG);
        snackbar.show();
        View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.transparente));
        TextView txtv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    // Revision de permisos para APIs superiores a 23
    private void verificarPermisos() {

        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionCheck2 = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED ) {
            pedirPermisos(0);
        } else if (permissionCheck2 != PackageManager.GET_URI_PERMISSION_PATTERNS){
            pedirPermisos(1);
        }
    }

    private void pedirPermisos(int a) {

        if (a == 0){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                Toast.makeText(this,R.string.permisosDenegados,Toast.LENGTH_SHORT).show();
                abrirConfiguracion();
            } else {

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_WRITE_EXTERNAL_STORAGE);
            }
        }

        if (a == 1){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE) ) {
                Toast.makeText(this,R.string.permisosDenegados,Toast.LENGTH_SHORT).show();
                abrirConfiguracion();
            } else {

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},
                        MY_WRITE_EXTERNAL_STORAGE);
            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[]grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setSnackBar2(mLayout,"");
            } else {
                mostrarConfiguración();
            }
        }
    }

    // Funcion de ordena los contactos por nombre
    public void ordenaPorNombre(View v){
        adaptador = new Adaptador(this, bdAgenda.recuperarCursosdeContactos());
        setListAdapter(adaptador);
        adaptador.notifyDataSetChanged();
        setSnackBar(mLayout,getString(R.string.tablaorden1));
    }

    // Funcion de ordena los contactos por nombre
    public void ordenaPorAntiguedad(View v){
        adaptador = new Adaptador(this, bdAgenda.recuperarCursosdeContactosPorAntiguedad());
        setListAdapter(adaptador);
        adaptador.notifyDataSetChanged();
        setSnackBar(mLayout,getString(R.string.tablaorden2));
    }

    // Revisa el TextInputLayout para el buscador
    private boolean nombreValido(String nombre) {
        Pattern patron = Pattern.compile("^[a-zA-Z ]+$");
        if (nombre.length() == 0){
            tp_nombre.setError(null);
        }
        else if (!patron.matcher(nombre).matches()) {
            tp_nombre.setError(getString(R.string.nm_inv));
            return false;
        } else {
            tp_nombre.setError(null);
        }

        return true;
    }

    // Crea el Drawer
    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        menuItem.getItemId();
                        return true;
                    }
                });
    }

    // Informa al usuario
    private void mostrarConfiguración() {
        Snackbar.make(mLayout, R.string.permiso_escritura,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.configurar, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        abrirConfiguracion();
                    }
                })
                .show();
    }

    // Abre la configuracion para establecer permisos
    public void abrirConfiguracion() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    // Dado un string de ruta devuelve un bitmap con la imagen
    private Bitmap recogerImagen(String c){
        File ruta = Environment.getExternalStorageDirectory();
        File file = new File(ruta.getAbsolutePath(), c);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return  bitmap;
    }

    // Guarda los datos del BottomSheet para mostrarlos cuando se gira el dispositivo
    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref_movil, btsheet_movil.getText().toString());
        editor.putString(pref_nombre, btsheet_nombre.getText().toString());
        editor.commit();
    }
}
