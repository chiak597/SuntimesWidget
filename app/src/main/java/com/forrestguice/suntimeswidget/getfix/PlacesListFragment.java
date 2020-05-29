/**
    Copyright (C) 2014-2020 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.getfix;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlacesListFragment extends Fragment
{
    protected PlacesListAdapter adapter;
    protected RecyclerView listView;
    protected ActionMode actionMode = null;
    protected PlacesListActionCompat actions = new PlacesListActionCompat();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_placeslist, parent, false);

        if (savedState != null) {
            // TODO
        }

        adapter = new PlacesListAdapter();
        adapter.setAdapterListener(listAdapterListener);

        listView = (RecyclerView) dialogContent.findViewById(R.id.placesList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);

        reloadAdapter();
        return dialogContent;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.placeslist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.clearPlaces:
                clearPlaces(getActivity());
                return true;

            case R.id.exportPlaces:
                exportPlaces(getActivity());
                return true;

            case R.id.addWorldPlaces:
                addWorldPlaces(getActivity());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean triggerActionMode(View view, PlaceItem item)
    {
        if (actionMode == null)
        {
            if (item != null)
            {
                adapter.setSelectedRowID(item.rowID);
                actions.setItem(item);

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                actionMode = activity.startSupportActionMode(actions);
                if (actionMode != null) {
                    actionMode.setTitle(item.location != null ? item.location.getLabel() : "");
                }
            }
            return true;

        } else {
            actionMode.finish();
            triggerActionMode(view, item);
            return false;
        }
    }

    private class PlacesListActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        private PlaceItem item = null;
        public void setItem(PlaceItem item) {
            this.item = item;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.placescontext, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            SuntimesUtils.forceActionBarIcons(menu);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.selectPlace:
                    selectPlace(item);
                    mode.finish();
                    return true;

                case R.id.editPlace:
                    editPlace(item);
                    return true;

                case R.id.deletePlace:
                    deletePlace(getActivity(), item);
                    mode.finish();
                    return true;

                case R.id.sharePlace:
                    sharePlace(item);
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
            adapter.setSelectedRowID(-1);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void reloadAdapter()
    {
        PlacesListTask listTask = new PlacesListTask(getActivity());
        listTask.setTaskListener(listTaskListener);
        listTask.execute();
    }

    protected PlacesListTask.TaskListener listTaskListener = new PlacesListTask.TaskListener()
    {
        @Override
        public void onStarted() {}

        @Override
        public void onFinished(List<PlaceItem> results) {
            adapter.setValues(results);
        }
    };

    protected PlacesListAdapter.AdapterListener listAdapterListener = new PlacesListAdapter.AdapterListener()
    {
        @Override
        public void onItemClicked(PlaceItem item, int position) {
            triggerActionMode(null, item);
        }
    };

    public void showProgress( Context context, CharSequence title, CharSequence message ) {
        //progress = ProgressDialog.show(context, title, message, true);
    }

    public void dismissProgress()
    {
        //if (!isDetached() && progress != null && progress.isShowing()) {
            //progress.dismiss();
        //}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void selectPlace(@Nullable PlaceItem item)
    {
        // TODO
        Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();
    }

    protected void sharePlace(@Nullable PlaceItem item)
    {
        if (item != null && item.location != null)
        {
            Intent intent = new Intent();
            intent.setData(item.location.getUri());
            startActivity(intent);
        }
    }

    protected void editPlace(@Nullable PlaceItem item)
    {
        // TODO
        Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void deletePlace(final Context context, @Nullable final PlaceItem item)
    {
        if (item != null && item.location != null)
        {
            AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.locationdelete_dialog_title))
                    .setMessage(context.getString(R.string.locationdelete_dialog_message, item.location.getLabel()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.locationdelete_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            DeletePlaceTask task = new DeletePlaceTask(context);
                            task.setTaskListener(new DeletePlaceTask.TaskListener()
                            {
                                @Override
                                public void onFinished(long rowID, boolean result) {
                                    adapter.removeValue(rowID);
                                }
                            });
                            task.execute(item.rowID);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.locationdelete_dialog_cancel), null);

            confirm.show();
        }
    }

    public static class DeletePlaceTask extends AsyncTask<Object, Object, Boolean>
    {
        private GetFixDatabaseAdapter database;
        private long rowID = -1;

        public DeletePlaceTask(Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(Object... params)
        {
            if (params.length > 0) {
                rowID = (Long)params[0];
            }
            if (rowID != -1)
            {
                database.open();
                boolean result = database.removePlace(rowID);
                database.close();
                return result;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (taskListener != null)
                taskListener.onFinished(rowID, result);
        }

        private TaskListener taskListener = null;
        public void setTaskListener( TaskListener listener ) {
            taskListener = listener;
        }
        public static abstract class TaskListener
        {
            public void onFinished( long rowID, boolean result ) {}
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearPlaces(final Context context)
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.locationclear_dialog_title))
                .setMessage(context.getString(R.string.locationclear_dialog_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(context.getString(R.string.locationclear_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        BuildPlacesTask task = new BuildPlacesTask(context);
                        task.setTaskListener(clearPlacesListener);
                        task.execute(true);   // clearFlag set to true
                    }
                })
                .setNegativeButton(context.getString(R.string.locationclear_dialog_cancel), null);

        confirm.show();
    }
    private BuildPlacesTask.TaskListener clearPlacesListener = new BuildPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            showProgress(getActivity(), context.getString(R.string.locationcleared_dialog_title), context.getString(R.string.locationcleared_dialog_message));
        }

        @Override
        public void onFinished(Integer result)
        {
            setRetainInstance(false);
            dismissProgress();
            Toast.makeText(getActivity(), getActivity().getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            reloadAdapter();
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void exportPlaces(Context context)
    {
        ExportPlacesTask task = new ExportPlacesTask(context, "SuntimesPlaces", true, true);  // export to external cache
        task.setTaskListener(exportPlacesListener);
        task.execute();
    }
    private ExportPlacesTask.TaskListener exportPlacesListener = new ExportPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            showProgress(getActivity(), context.getString(R.string.locationexport_dialog_title), context.getString(R.string.locationexport_dialog_message));
        }

        @Override
        public void onFinished(ExportPlacesTask.ExportResult results)
        {
            setRetainInstance(false);
            dismissProgress();

            if (results.getResult())
            {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType(results.getMimeType());
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    //Uri shareURI = Uri.fromFile(results.getExportFile());  // this URI works until api26 (throws FileUriExposedException)
                    Uri shareURI = FileProvider.getUriForFile(getActivity(), "com.forrestguice.suntimeswidget.fileprovider", results.getExportFile());
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);

                    String successMessage = getActivity().getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                    Toast.makeText(getActivity().getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                    getActivity().startActivity(Intent.createChooser(shareIntent, getActivity().getResources().getText(R.string.msg_export_to)));
                    return;   // successful export ends here...

                } catch (Exception e) {
                    Log.e("ExportPlaces", "Failed to share file URI! " + e);
                }
            }

            File file = results.getExportFile();    // export failed
            String path = ((file != null) ? file.getAbsolutePath() : "<path>");
            String failureMessage = getActivity().getString(R.string.msg_export_failure, path);
            Toast.makeText(getActivity().getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void addWorldPlaces(Context context)
    {
        BuildPlacesTask task = new BuildPlacesTask(context);
        task.setTaskListener(buildPlacesListener);
        task.execute();
    }
    private BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            setRetainInstance(true);
            Context context = getActivity();
            showProgress(getActivity(), context.getString(R.string.locationbuild_dialog_title), context.getString(R.string.locationbuild_dialog_message));
        }

        @Override
        public void onFinished(Integer result)
        {
            setRetainInstance(false);
            dismissProgress();
            if (result > 0)
            {
                reloadAdapter();
                Toast.makeText(getActivity(), getActivity().getString(R.string.locationbuild_toast_success, result.toString()), Toast.LENGTH_LONG).show();
            } // else // TODO: fail msg
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListTask extends AsyncTask<Void, Location, List<PlaceItem>>
    {
        protected GetFixDatabaseAdapter database;

        public PlacesListTask(Context context) {
            database = new GetFixDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected List<PlaceItem> doInBackground(Void... voids)
        {
            if (listener != null) {
                listener.onStarted();
            }

            ArrayList<PlaceItem> result = new ArrayList<>();

            database.open();
            Cursor cursor = database.getAllPlaces(0, true);
            if (cursor != null)
            {
                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    String name = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_NAME));
                    String lat = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LATITUDE));
                    String lon = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LONGITUDE));
                    String alt = cursor.getString(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_ALTITUDE));
                    Location location = new Location(name, lat, lon, alt);
                    location.setUseAltitude(true);
                    result.add(new PlaceItem(cursor.getLong(cursor.getColumnIndex(GetFixDatabaseAdapter.KEY_ROWID)), location));
                    cursor.moveToNext();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<PlaceItem> result)
        {
            if (listener != null) {
                listener.onFinished(result);
            }
        }

        protected TaskListener listener = null;
        public void setTaskListener(TaskListener listener) {
            this.listener = listener;
        }

        public interface TaskListener
        {
            void onStarted();
            void onFinished(List<PlaceItem> results);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListAdapter extends RecyclerView.Adapter<PlacesListViewHolder>
    {
        protected ArrayList<PlaceItem> items = new ArrayList<>();

        public void setValues(List<PlaceItem> values)
        {
            items.clear();
            items.addAll(values);
            notifyDataSetChanged();
        }

        public void removeValue(long rowID)
        {
            int position = -1;
            for (int i=0; i<items.size(); i++)
            {
                PlaceItem item = items.get(i);
                if (item != null && item.rowID == rowID) {
                    position = i;
                    break;
                }
            }
            if (position != -1)
            {
                items.remove(position);
                notifyItemRemoved(position);
            }
        }

        private long selectedRowID = -1;
        public void setSelectedRowID( long rowID )
        {
            selectedRowID = rowID;
            notifyDataSetChanged();
        }
        public long getSelectedRowID() {
            return selectedRowID;
        }
        public void clearSelection() {
            setSelectedRowID(-1);
        }

        @Override
        public PlacesListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_listitem_places, parent, false);
            return new PlacesListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlacesListViewHolder holder, int position)
        {
            PlaceItem item = items.get(position);
            holder.selected = (item.rowID == selectedRowID);
            holder.bindViewHolder(item);
            attachClickListeners(holder, position);
        }

        @Override
        public void onViewRecycled(PlacesListViewHolder holder)
        {
            detachClickListeners(holder);
            holder.unbindViewHolder();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        protected AdapterListener listener = null;
        public void setAdapterListener(AdapterListener listener) {
            this.listener = listener;
        }

        protected void attachClickListeners(PlacesListViewHolder holder, int position)
        {
            if (holder.card != null) {
                holder.card.setOnClickListener(onItemClicked(position));
            }
        }

        protected View.OnClickListener onItemClicked(final int position)
        {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(items.get(position), position);
                    }
                }
            };
        }

        protected void detachClickListeners(PlacesListViewHolder holder)
        {
            if (holder.card != null) {
                holder.card.setOnClickListener(null);
            }
        }

        public interface AdapterListener {
            void onItemClicked(PlaceItem item, int position);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlacesListViewHolder extends RecyclerView.ViewHolder
    {
        public View card;
        public TextView label;
        public TextView summary;
        public boolean selected = false;

        public int color_selected = Color.GREEN;

        public PlacesListViewHolder(View itemView)
        {
            super(itemView);
            card = itemView.findViewById(R.id.listitem_layout);
            label = (TextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
        }

        public void bindViewHolder( PlaceItem item )
        {
            if (card != null) {
                // TODO
                //card.setBackgroundColor(selected ? color_selected : Color.BLACK);
            }
            if (label != null) {
                label.setText(item != null && item.location != null ? item.location.getLabel() : "");
            }
            if (summary != null) {
                summary.setText(item != null && item.location != null ? item.location.getLatitude() + ", " + item.location.getLongitude() + " (TODO)" : "");  // TODO
            }
        }

        public void unbindViewHolder() {
            selected = false;
            bindViewHolder(null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PlaceItem
    {
        public long rowID = -1;
        public Location location = null;

        public PlaceItem() {}

        public PlaceItem( long rowID, Location location )
        {
            this.rowID = rowID;
            this.location = location;
        }
    }

}
