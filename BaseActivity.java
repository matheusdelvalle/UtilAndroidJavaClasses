package br.com.dalcatech.allfood.myapplication.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.com.dalcatech.allfood.myapplication.R;
import br.com.dalcatech.allfood.myapplication.fragment.BaseFragment;
import br.com.dalcatech.allfood.myapplication.utils.ActivityEffectFactory;

/**
 * Created by Gustavo on 10/05/2016.
 */
public class BaseActivity extends livroandroid.lib.activity.BaseActivity {
    private Map<String, AsyncTask> tasks = new HashMap<String, AsyncTask>();
    private ProgressDialog progress;


    public void setupToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public void hideToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void startTask(String cod, TaskListener listener) {
        startTask(cod, listener, 0);
    }

    public void startTask(String cod, TaskListener listener, int progressId) {
        Log.d("livroandroid", "startTask: " + cod);
        /*View view = getWindow().getDecorView().getRootView();
        if (view == null) {
            throw new RuntimeException("Somente pode iniciar a task se a view do fragment foi criada.\nChame o startTask depois do onCreateView");
        }*/

        Task task = (Task) this.tasks.get(cod);
        if (task == null) {
            // Somente executa se já não está executando
            task = new Task(cod, listener, progressId);
            this.tasks.put(cod, task);
            task.execute();
        }
    }

    private class Task extends AsyncTask<Object, Void, TaskResult> {

        private String cod;
        private TaskListener listener;
        private int progressId;

        private Task(String cod, TaskListener listener, int progressId) {
            this.cod = cod;
            this.listener = listener;
            this.progressId = progressId;
        }

        @Override
        protected void onPreExecute() {
            Log.d("livroandroid", "task onPreExecute()");
//            showProgress(this, progressId);
        }

        @Override
        protected TaskResult doInBackground(Object... params) {
            TaskResult r = new TaskResult();
            try {
                r.response = listener.execute();
            } catch (Exception e) {
                Log.e("livroandroid", e.getMessage(), e);
                r.exception = e;
            }
            return r;
        }

        protected void onPostExecute(TaskResult result) {
            Log.d("livroandroid", "task onPostExecute(): " + result);
            try {
                if (result != null) {
                    if (result.exception != null) {
                        listener.onError(result.exception);
                    } else {
                        listener.updateView(result.response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                tasks.remove(cod);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            tasks.remove(cod);
            listener.onCancelled(cod);
        }
    }

    private class TaskResult<T> {
        private T response;
        private Exception exception;
    }

    public interface TaskListener<T> {
        // Executa em background e retorna o objeto
        T execute() throws Exception;

        // Atualiza a view na UI Thread
        void updateView(T response) throws IOException, Settings.SettingNotFoundException;

        // Chamado caso o método execute() lance uma exception
        void onError(Exception exception);

        // Chamado caso a task tenha sido cancelada
        void onCancelled(String cod);
    }

    protected void showProgress(final Task task, int progressId, View v) {
        if (progressId > 0 && v != null) {
            View view = v.findViewById(progressId);
            if (view != null) {
                if (view instanceof SwipeRefreshLayout) {
                    SwipeRefreshLayout srl = (SwipeRefreshLayout) view;
                    if (!srl.isRefreshing()) {
                        srl.setRefreshing(true);
                    }
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }

    private void closeProgress(int progressId, View v) {
        if (progressId > 0 && v != null) {
            View view = v.findViewById(progressId);
            if (view != null) {
                if (view instanceof SwipeRefreshLayout) {
                    SwipeRefreshLayout srl = (SwipeRefreshLayout) view;
                    srl.setRefreshing(false);
                } else {
                    view.setVisibility(View.GONE);
                }
                return;
            }
        }

        Log.d("livroandroid", "closeProgress()");
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void replaceFragment(Fragment fragment, int container) {
        if (fragment != null) {
            // replace
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

            Fragment oldFragment = getContentFragment();
            if (oldFragment != null && oldFragment instanceof BaseFragment) {
                BaseFragment base = (BaseFragment) oldFragment;
                base.release();
            }

            fragmentManager.beginTransaction().replace(container, fragment, fragment.getTag()).commit();
        }
    }

    public Fragment getContentFragment() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        return (Fragment) fragmentManager.findFragmentByTag("homeFragment");
    }

    public static void showTop(Activity context, Class<? extends Activity> cls) {
        showTop(context, cls, (Bundle) null);
    }

    public static void showTop(Activity context, Class<? extends Activity> cls, Bundle param) {
        Intent intent = new Intent(context, cls);
        if (param != null) {
            intent.putExtras(param);
        }

        intent.setFlags(67108864);
        if (context != null) {
            context.startActivity(intent);
            ActivityEffectFactory.get().apply(context);
        }

    }

    public static void show(Activity context, Class<? extends Activity> cls) {
        show(context, cls, (Bundle) null);
    }

    public static void show(Activity context, Class<? extends Activity> cls, Bundle params) {
        Intent intent = new Intent(context, cls);
        if (params != null) {
            intent.putExtras(params);
        }

        if (context != null) {
            context.startActivity(intent);
            ActivityEffectFactory.get().apply(context);
        }

    }

    public static void showForResult(Activity context, Class<? extends Activity> cls, Bundle params, int requestCode) {
        Intent intent = new Intent(context, cls);
        if (params != null) {
            intent.putExtras(params);
        }

        if (context != null) {
            context.startActivityForResult(intent, requestCode);
        }

    }

    protected void snack(View view, String msg, String button,final Runnable runnable) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
            public void onClick(View v) {
                if(runnable != null) {
                    runnable.run();
                }

            }
        }).show();
    }

}
