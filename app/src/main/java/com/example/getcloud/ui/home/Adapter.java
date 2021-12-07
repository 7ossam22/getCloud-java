package com.example.getcloud.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.getcloud.R;
import com.example.getcloud.pojo.File_Data;
import com.example.getcloud.ui.login.MainActivity;
import com.example.getcloud.ui.signup.CreateAccount;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Adapter extends FirestoreRecyclerAdapter<File_Data, Adapter.MyViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    AlertDialog alertDialog;
    AlertDialog.Builder alertdialogbuilder;


    String UID;
    int TOTAL_SIZE = 0;

    public Adapter(@NonNull FirestoreRecyclerOptions<File_Data> options, Context context, String UID) {
        super(options);
        this.context = context;
        this.UID = UID;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull File_Data model) {
        holder.ITEM_NAME.setText(model.getName());
        holder.ITEM_DATE.setText(model.getDate());

        String url = model.getLink();
        String type = model.getType();
        double size = Double.parseDouble(model.getSize());

        sizecalculation(size, holder.ITEM_SIZE);

        if (type.startsWith("image")) {
            Glide.with(context)
                    .load(url)
                    .optionalCircleCrop()
                    .placeholder(R.drawable.ic_baseline_cloud_24)
                    .into(holder.ITEM_IMAGE);
        } else if (type.endsWith("android.package-archive")) {
            holder.ITEM_IMAGE.setImageResource(R.drawable.ic_baseline_android_24);
        } else if (type.endsWith("pdf")) {
            holder.ITEM_IMAGE.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24);
        } else if (type.startsWith("audio")) {
            holder.ITEM_IMAGE.setImageResource(R.drawable.ic_baseline_audiotrack_24);
        } else {
            holder.ITEM_IMAGE.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }

        //menu clicking
        holder.ITEM_MORE.setOnClickListener((View.OnClickListener) v -> {
            PopupMenu menu = new PopupMenu(context, holder.ITEM_MORE);
            menu.getMenuInflater().inflate(R.menu.item_menu, menu.getMenu());
            menu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) item -> {
                switch (item.getItemId()) {
                    case R.id.download:
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            onDownloadItem(context, model.getLink(), model.getName());
                        } else {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9);
                        }
                        break;

                    case R.id.Delete:

                        alertdialogbuilder = new AlertDialog.Builder(context);
                        alertdialogbuilder.setTitle("Deleting ...");
                        alertdialogbuilder.setIcon(R.drawable.ic_baseline_delete_forever_24);
                        alertdialogbuilder.setMessage("Are you sure you want to delete this Data?!");
                        alertdialogbuilder.setCancelable(false);
                        alertdialogbuilder.setPositiveButton("Yes", (dialogInterface, i) -> {
                            String docID = getSnapshots().getSnapshot(position).getId();
                            System.out.println(docID);
                            db.collection("Users").document(UID).collection("Data").document(docID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        System.out.println("failed");
                                    } else {
                                        System.out.println("Deleted");
                                    }
                                }
                            });
                        });
                        alertdialogbuilder.setNegativeButton("No", (dialogInterface, i) -> alertdialogbuilder.setCancelable(true));
                        alertDialog = alertdialogbuilder.create();
                        alertDialog.show();


                        break;
                }
                return true;
            });
            menu.show();
        });
        //we Done Here YAAAAAAAAAAAAAAY :D

        TOTAL_SIZE += (int) size;
        db.collection("Users").document(UID).update("Usage", String.valueOf(TOTAL_SIZE)).addOnCompleteListener(task1 -> {
            if (!task1.isSuccessful()) {
                Toast.makeText(context, "Failed to push your Data ", Toast.LENGTH_LONG).show();
            } else {
                TOTAL_SIZE = 0;
            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView ITEM_NAME, ITEM_DATE, ITEM_SIZE;
        ImageView ITEM_IMAGE, ITEM_MORE;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ITEM_NAME = itemView.findViewById(R.id.item_name);
            ITEM_DATE = itemView.findViewById(R.id.item_date);
            ITEM_IMAGE = itemView.findViewById(R.id.item_image);
            ITEM_SIZE = itemView.findViewById(R.id.item_size);
            ITEM_MORE = itemView.findViewById(R.id.item_more);
        }
    }


    public void sizecalculation(double size, TextView tv) {
        double size_kb = size / 1024;
        double size_mb = size_kb / 1024;
        double size_gb = size_mb / 1024;
        double size_tb = size_gb / 1024;

        if (size_tb >= 1) {
            tv.setText((int) size_tb + "TB");
        } else if (size_gb >= 1) {
            tv.setText((int) size_gb + "GB");
        } else if (size_mb >= 1) {
            tv.setText((int) size_mb + "MB");
        } else if (size_kb >= 1) {
            tv.setText((int) size_kb + "KB");
        } else {
            tv.setText((int) size + "bytes");
        }
    }

    public void onDownloadItem(Context context, String link, String name) {
        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(link);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(name);
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        downloadmanager.enqueue(request);
    }
}
