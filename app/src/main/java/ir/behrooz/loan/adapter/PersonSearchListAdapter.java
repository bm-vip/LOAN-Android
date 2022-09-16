package ir.behrooz.loan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.model.PersonModel;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
public class PersonSearchListAdapter extends ArrayAdapter<PersonModel> {

    private static class ViewHolder {
        TextView fullName;
        CheckBox checkBox;
    }

    public PersonSearchListAdapter(Context context, List<PersonModel> personModelList) {
        super(context, R.layout.list_item_person_search, personModelList);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final PersonModel personModel = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_person_search, parent, false);
            new FontChangeCrawler(getContext().getAssets()).replaceFonts((ViewGroup) convertView);
            viewHolder.fullName = (TextView) convertView.findViewById(R.id.fullName);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fullName.setText(personModel.getFullName());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personModel.setChecked(viewHolder.checkBox.isChecked());
            }
        });
        return convertView;
    }
}
