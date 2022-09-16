package ir.behrooz.loan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.model.MenuModel;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
public class MenuListAdapter extends ArrayAdapter<MenuModel> {

    private Context context;

    private static class ViewHolder {
        TextView text;
        ImageView icon;
        LinearLayout menu;
    }

    public MenuListAdapter(Context context, List<MenuModel> menuModels) {
        super(context, R.layout.list_item_menu, menuModels);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final MenuModel menuModel = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_menu, parent, false);
            new FontChangeCrawler(getContext().getAssets()).replaceFonts((ViewGroup) convertView);
            viewHolder.text = convertView.findViewById(R.id.menu_text);
            viewHolder.icon = convertView.findViewById(R.id.menu_icon);
            viewHolder.menu = convertView.findViewById(R.id.menu_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text.setText(menuModel.getTitle());
        viewHolder.icon.setBackgroundResource(menuModel.getIcon());
        viewHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menuModel.getIntent() == null) {
                    Intent intent = new Intent(context, menuModel.getActivity());
                    for (Map.Entry<String, Long> entry : menuModel.getExtera().entrySet()) {
                        intent.putExtra(entry.getKey(), entry.getValue());
                    }
                    context.startActivity(intent);
                } else {
                    context.startActivity(menuModel.getIntent());
                }
            }
        });
        return convertView;
    }
}
