package benliu.com.test.currencyconverter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by beliu on 2017-06-15.
 */

public class CustomViewHolder extends RecyclerView.ViewHolder {

    public TextView nameView;
    public TextView rateView;


    public CustomViewHolder(View itemView) {
        super(itemView);
        nameView = (TextView) itemView.findViewById(R.id.name);
        rateView = (TextView) itemView.findViewById(R.id.rate);

    }

    public void setData(Cursor c, double ratio) {
        nameView.setText(c.getString(c.getColumnIndex(TableItems.NAME)));
        rateView.setText(String.format("%.2f", c.getDouble(c.getColumnIndex(TableItems.RATE))*ratio));

    }
}
