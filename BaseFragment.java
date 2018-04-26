package br.com.dalcatech.allfood.myapplication.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import br.com.dalcatech.allfood.myapplication.utils.ActivityEffectFactory;
import livroandroid.lib.fragment.DebugFragment;

/**
 * Created by Gustavo on 10/05/2016.
 */
public class BaseFragment extends DebugFragment {

    private Map<String, Task> tasks = new HashMap<String, Task>();
    private ProgressDialog progress;
    private SwipeRefreshLayout swipeLayout;

    public void release() {

    }

    protected void snack(View view, int msg, Runnable runnable) {
        this.snack(view, this.getString(msg), runnable);
    }

    protected void snack(View view, int msg) {
        this.snack(view, this.getString(msg), (Runnable)null);
    }

    protected void snack(View view, String msg) {
        this.snack(view, msg, (Runnable)null);
    }

    protected void snack(View view, String msg, final Runnable runnable) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Ok", new View.OnClickListener() {
            public void onClick(View v) {
                if(runnable != null) {
                    runnable.run();
                }

            }
        }).show();
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

    protected void showView(int viewId) {
        View view = this.getView();
        this.showView(view, viewId);
    }

    protected void showView(View view, int viewId) {
        if(view != null) {
            this.showView(view.findViewById(viewId));
        }

    }

    protected void showView(final View view) {
        if(view != null) {
            FragmentActivity activity = this.getActivity();
            if(activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        view.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

    }

    protected void goneView(int viewId) {
        View view = this.getView();
        this.goneView(view, viewId);
    }

    protected void goneView(View view, int viewId) {
        if(view != null) {
            this.goneView(view.findViewById(viewId));
        }

    }

    protected void goneView(final View view) {
        if(view != null) {
            FragmentActivity activity = this.getActivity();
            if(activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        view.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }

    }

    public Context getContext() {
        return getActivity();
    }

    public void startTask(String cod, TaskListener listener, boolean showProgress) {
        startTask(cod, listener, 0, showProgress);
    }

    public void startTask(String cod, TaskListener listener) {
        startTask(cod, listener, 0, true);
    }

    public void startTask(String cod, TaskListener listener, int progressId) {
        startTask(cod, listener, progressId, true);
    }

    protected void startTask(String cod, TaskListener listener, int progressId, boolean showProgress) {
        Log.d("livroandroid", "startTask: " + cod);
        View view = getView();
        if (view == null) {
            throw new RuntimeException("Somente pode iniciar a task se a view do fragment foi criada.\nChame o startTask depois do onCreateView");
        }

        Task task = this.tasks.get(cod);
        if (task == null) {
            // Somente executa se já não está executando
            task = new Task(cod, listener, progressId, showProgress);
            this.tasks.put(cod, task);
            task.execute();
        }
    }

    public void cancellTask(String cod) {
        Task task = tasks.get(cod);
        if (task != null) {
            task.cancel(true);
            tasks.remove(cod);
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
        void updateView(T response);

        // Chamado caso o método execute() lance uma exception
        void onError(Exception exception);

        // Chamado caso a task tenha sido cancelada
        void onCancelled(String cod);
    }

    /**
     * Implementa a interface com métodos vazios.
     *
     * @param <T>
     */
    public class BaseTask<T> implements TaskListener<T> {

        @Override
        public T execute() throws Exception {
            return null;
        }

        @Override
        public void updateView(T response) {

        }

        @Override
        public void onError(Exception exception) {
            alert(exception.getMessage());
        }

        @Override
        public void onCancelled(String cod) {

        }
    }

    private class Task extends AsyncTask<Void, Void, TaskResult> {

        private String cod;
        private TaskListener listener;
        private int progressId;
        private boolean showProgress;

        private Task(String cod, TaskListener listener, int progressId, boolean showProgress) {
            this.cod = cod;
            this.listener = listener;
            this.progressId = progressId;
            this.showProgress = showProgress;
        }

        @Override
        protected void onPreExecute() {
            Log.d("livroandroid", "task onPreExecute()");
            showProgress(this, progressId, showProgress);
        }

        @Override
        protected TaskResult doInBackground(Void... params) {
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
            } finally {
                tasks.remove(cod);
                closeProgress(progressId);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            tasks.remove(cod);
            listener.onCancelled(cod);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        stopTasks();
    }

    private void stopTasks() {
        if (tasks != null) {
            for (String key : tasks.keySet()) {
                Task task = tasks.get(key);
                if (task != null) {
                    boolean running = task.getStatus().equals(AsyncTask.Status.RUNNING);
                    if (running) {
                        task.cancel(true);
                        closeProgress(0);
                    }
                }
            }
            tasks.clear();
        }
    }

    private void closeProgress(int progressId) {
        if (progressId > 0 && getView() != null) {
            View view = getView().findViewById(progressId);
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

    protected void showProgress(final Task task, int progressId, boolean showProgress) {
        if(!showProgress) {
            return;
        }

        if (progressId > 0 && getView() != null) {
            View view = getView().findViewById(progressId);
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

        // Mostra o dialog e permite cancelar
        if (progress == null) {
            progress = ProgressDialog.show(getActivity(), "Aguarde", "Por favor aguarde...");
            progress.setCancelable(true);
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancela a AsyncTask
                    task.cancel(true);
                }
            });
        }
    }

    protected void setTextString(int resId, String text) {
        View view = getView();
        if (view != null) {
            TextView t = (TextView) view.findViewById(resId);
            if (t != null) {
                t.setText(text);
            }
        }
    }

    protected String getTextString(int resId) {
        View view = getView();
        if (view != null) {
            TextView t = (TextView) view.findViewById(resId);
            if (t != null) {
                return t.getText().toString();
            }
        }
        return null;
    }

    protected void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    protected void toast(int msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    protected void alert(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    protected void alert(int msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    protected void log(String msg) {
        Log.d(TAG, msg);
    }

    public android.support.v7.app.ActionBar getActionBar() {
        AppCompatActivity ac = getAppCompatActivity();
        return ac.getSupportActionBar();
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
