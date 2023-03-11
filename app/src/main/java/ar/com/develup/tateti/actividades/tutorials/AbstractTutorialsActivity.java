package ar.com.develup.tateti.actividades.tutorials;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import ar.com.develup.tateti.R;


public abstract class AbstractTutorialsActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {


    FloatingActionButton fab_button;
    ViewPager pager;
    TabLayout tabLayout;
    TutorialAdapter pagerAdapter;

    ArrayList<Fragment> fragmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_abstract);

        // Oculto la barra superior
       // Objects.requireNonNull(getSupportActionBar()).hide();

        // Fragmentos
        initializeFragments();

        // Pager
        initializePagers();

        // Botón para finalizar
        initializeFloatingActionButton();

        /*
        if (fragmentsList.size() > 0) {
            onPageSelected(0);
        } else {
            this.finish();
        }
        */
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if (pagerAdapter.hasNext(pager.getCurrentItem())) {
            //this.fab_button.setVisibility(View.GONE);
            this.fab_button.setImageResource(R.drawable.icon_next);
        } else {
            //this.fab_button.setVisibility(View.VISIBLE);
            this.fab_button.setImageResource(R.drawable.icon_check);
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public void onBackPressed() {
        if (this.pager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);
        }
    }


    /* ---------- Protected --------------------------------------------------------------------- */


    /**
     * Se debe redefinir en las subclases
     */
    protected abstract void initializeFragments();

    /**
     * Se debe redefinir en las subclases
     */
    protected abstract void handleLastFragmentClosed();


    /**
     * Agrega un nuevo fragmento al array
     *
     * @param color_string
     * @param image
     * @param title
     * @param content
     */
    protected void addFragment(String color_string, int image, String title, String content) {
        fragmentsList.add(
                TutorialFragment.newInstance(

                        //NArias: no logré que funcione mandandole un color: R.color.xxxx, por eso le mando el string
                        Color.parseColor(color_string),
                        fragmentsList.size(),
                        image,
                        title,
                        content)
        );
    }

    /**
     * Sobrecarga
     *
     * @param image
     * @param title
     * @param content
     */
    protected void addFragment(int image, String title, String content) {
        this.addFragment("#FFFFFF", image, title, content);
    }

    /**
     * Inicializa los controles que permiten navegar entre los fragmentos
     */
    protected void initializePagers() {

        this.pagerAdapter = new TutorialAdapter(getSupportFragmentManager(), this.fragmentsList);

        this.pager = (ViewPager) findViewById(R.id.pager);
        this.pager.setAdapter(this.pagerAdapter);
        this.pager.addOnPageChangeListener(this);

        tabLayout = (TabLayout) findViewById(R.id.titles);
    }


    /**
     * Inicializa el control que permite finalizar el tutorial
     */
    protected void initializeFloatingActionButton() {
        fab_button = (FloatingActionButton) findViewById(R.id.fab_button);

        fab_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AbstractTutorialsActivity.this.handleFabButtonClick();
            }
        });
    }


    /**
     *
     */
    protected void handleFabButtonClick() {
        int i = pager.getCurrentItem();
        if (pagerAdapter.hasNext(i)) {
            pager.setCurrentItem(i + 1);
        } else {
            this.handleLastFragmentClosed();
            this.finish();
        }
    }



    /* ------------------------------------------------------------------------------------------ */

    /**
     * Clase Privada
     * <p>
     * Sirve de Adapter para el array de fragmentos
     */
    private class TutorialAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragmentsList;

        public TutorialAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragmentsList = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return this.fragmentsList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        public boolean hasNext(int i) {
            return i + 1 < getCount();
        }
    }

    /* ------------------------------------------------------------------------------------------------- */

    /**
     * Clase Privada
     * <p>
     * La utilizo para los fragmentos que se muestran como tutorial
     */
    public static class TutorialFragment extends Fragment {
        private static final String BACKGROUND_COLOR = "color"; // key para color de fondo en el bundle
        private static final String INDEX = "index"; // key para indice de la página en el bundle
        private static final String IMAGE = "image"; // key para imagen en el bundle
        private static final String TITLE = "title"; // key para titulo en el bundle
        private static final String MESSAGE = "msg"; // key para mensaje en el bundle

        private int index;
        private int color;
        private int res_img;
        private String txt_message;
        private String txt_title;


        public static TutorialFragment newInstance(int color, int index, int res_img, String txt_title, String txt_message) {
            TutorialFragment fragment = new TutorialFragment();

            //Guardo los parametros
            Bundle bundle = new Bundle();
            bundle.putInt(BACKGROUND_COLOR, color);
            bundle.putInt(INDEX, index);
            bundle.putInt(IMAGE, res_img);
            bundle.putString(MESSAGE, txt_message);
            bundle.putString(TITLE, txt_title);
            fragment.setArguments(bundle);
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            super.onActivityCreated(savedInstanceState);

            //Cargo los parametros cuando la creacion inicial del fragmento termina
            this.color = (getArguments() != null) ? getArguments().getInt(BACKGROUND_COLOR) : Color.GRAY;
            this.index = (getArguments() != null) ? getArguments().getInt(INDEX) : -1;
            this.res_img = getArguments().getInt(IMAGE);
            this.txt_message = getArguments().getString(MESSAGE);
            this.txt_title = getArguments().getString(TITLE);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //todo pasar a binding
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_tutorial_fragment, container, false);

            // Obtengo los componentes de la vista
            TextView tv_title = (TextView) rootView.findViewById(R.id.tutorial_fragment_title);
            TextView tv_details = (TextView) rootView.findViewById(R.id.tutorial_fragment_details);
            ImageView image_view = (ImageView) rootView.findViewById(R.id.tutorial_fragment_image);

            // Seteo los valores
            tv_title.setText(txt_title);
            tv_details.setText(txt_message);
            image_view.setImageBitmap(BitmapFactory.decodeResource(getResources(), this.res_img));


            //Cambio el color de fondo
            rootView.setBackgroundColor(this.color);

            return rootView;
        }
    }

}


