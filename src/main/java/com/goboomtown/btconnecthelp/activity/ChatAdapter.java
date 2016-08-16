package com.goboomtown.btconnecthelp.activity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.goboomtown.btconnecthelp.R;
import com.goboomtown.btconnecthelp.api.BTConnectAPI;
import com.goboomtown.btconnecthelp.widget.WebImageView;
import com.goboomtown.chat.BoomtownChat;
import com.goboomtown.chat.BoomtownChatMessage;


/**
 * Created by Larry Borsato on 2016-07-12.
 */
public class ChatAdapter extends BaseAdapter {

    private final List<BoomtownChatMessage> chatMessages;
    public ChatFragment chatFragment;
    private Activity context;
    Bitmap bitmap;
    ProgressDialog pDialog;
    ViewHolder holder;
    BoomtownChatMessage chatMessage;
    Bitmap bmp;
    Bitmap bmpRight;
    private WebViewFragment fragmentWebView;


    public ChatAdapter(Activity context, List<BoomtownChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        BoomtownChat.sharedInstance().avatars = new HashMap<String, Object>();
        fragmentWebView = new WebViewFragment();
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public BoomtownChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final BoomtownChatMessage chatMessage = getItem(position);
//        chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_chat_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean isMe = chatMessage.isMe() ;//Just a dummy check to simulate whether it me or other sender
        setAlignment(holder, isMe);
        String message = chatMessage.getMessage();
        if ( message!=null && !message.isEmpty() ) {
            holder.contentWithBG.setVisibility(View.VISIBLE);
            holder.txtMessage.setText(chatMessage.getMessage());
        } else {
            holder.contentWithBG.setVisibility(View.GONE);
        }
        holder.txtInfo.setText(chatMessage.fromName + "  " + chatMessage.getHumanDate());
        holder.btnInfo.setText(chatMessage.fromName + "  " + chatMessage.getHumanDate());
        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Button btn = (Button) v;
//                chatFrament.addMention(btn.getText().toString());
                chatFragment.addMention(btn.getText().toString());
            }
        });

        holder.avatarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFragment.addMention(holder.btnInfo.getText().toString());
            }
        });

        holder.avatarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFragment.addMention(holder.btnInfo.getText().toString());
            }
        });

        if ( chatMessage.preview != null ) {
            holder.attachment.setVisibility(View.VISIBLE);
            holder.attachment.mRectangular = true;
            holder.attachment.setImageUrl(chatMessage.preview);
            holder.attachment.invalidate();
            if ( chatMessage.url != null ) {
                holder.imageUrl = chatMessage.url;
                holder.attachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d("ChatFragment", holder.imageUrl);
//                                fragmentWebView.url = holder.imageUrl;
//                                ((BaseActivity)context).getSupportFragmentManager().beginTransaction()
//                                        .add(fragmentWebView, "web")
//                                        .addToBackStack(null)
//                                        .commit();
                                chatFragment.showWebView(holder.imageUrl);
                            }
                        });
                     }
                });
            }
        }
        else holder.attachment.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = getAvatarForUser(chatMessage);
                 context.runOnUiThread(new Runnable() {
                     public void run() {
                         if (bmp != null) {
                             holder.avatarLeft.setImageBitmap(bmp);
                             holder.avatarRight.setImageBitmap(bmp);
                         }
                     }
                 });
                //                scroll();
            }
        }).start();

        return convertView;
    }

    public void add(BoomtownChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<BoomtownChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentPanel.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentPanel.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);
            layoutParams = (LinearLayout.LayoutParams) holder.attachment.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.attachment.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.btnInfo.setLayoutParams(layoutParams);
            holder.txtInfo.setLayoutParams(layoutParams);
            holder.avatarLeft.setVisibility(View.INVISIBLE);
            holder.avatarRight.setVisibility(View.VISIBLE);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentPanel.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentPanel.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);
            layoutParams = (LinearLayout.LayoutParams) holder.attachment.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.attachment.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.btnInfo.setLayoutParams(layoutParams);
            holder.txtInfo.setLayoutParams(layoutParams);
            holder.avatarLeft.setVisibility(View.VISIBLE);
            holder.avatarRight.setVisibility(View.INVISIBLE);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentPanel = (LinearLayout) v.findViewById(R.id.contentPanel);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.btnInfo = (Button) v.findViewById(R.id.btnInfo);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.avatarLeft = (ImageView) v.findViewById(R.id.avatarLeft);
        holder.avatarRight = (ImageView) v.findViewById(R.id.avatarRight);
        holder.attachment = (WebImageView) v.findViewById(R.id.attachment);
        holder.handle = null;
        holder.imageUrl = null;
        return holder;
    }


    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public Button btnInfo;
        public LinearLayout content;
        public LinearLayout contentPanel;
        public LinearLayout contentWithBG;
        public ImageView avatarLeft;
        public ImageView avatarRight;
        public String handle;
        public String imageUrl;
        public WebImageView attachment;
    }

    public Bitmap getAvatarForUser(BoomtownChatMessage chatMessage)
    {
        String urlString = null;
        Bitmap avatar = (Bitmap) BoomtownChat.sharedInstance().avatars.get(chatMessage.from);
        if (avatar != null)
            return avatar;

        if ( chatMessage.avatar!=null && !chatMessage.avatar.isEmpty() ) {
            urlString = chatMessage.avatar;
        } else {
            String resource = chatMessage.from;
            if (resource == null)
                return null;

            String[] tokens = resource.split(";");
            String from = tokens[0];

            tokens = from.split(":");

//            if ( tokens.length > 1 )
//                urlString = BTConnectAPI.BTConnectAPIBaseURL + "/api/v1/avatar/" + tokens[0] + "/" + tokens[1] + "/50,50";
        }
        URL url = null;
        try {
            url = new URL(urlString);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            avatar = getclip(BitmapFactory.decodeStream(is));
            BoomtownChat.sharedInstance().avatars.put(chatMessage.from, avatar);
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return avatar;
    }

    public Bitmap getclip(Bitmap bitmap) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width / 2, height / 2,
                width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }



}
