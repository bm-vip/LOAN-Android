package ir.behrooz.loan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.CompleteListener;

import static ir.behrooz.loan.common.Utils.getVersion;

/**
 * Created by Behrooz Mohamadi on 16/10/28.
 */
public class AboutFragment extends DialogFragment {
    private CompleteListener completeListener;

    public static AboutFragment newInstance() {
        AboutFragment frag = new AboutFragment();
        return frag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView version = view.findViewById(R.id.version);
        version.setText(String.format("%s %s", getText(R.string.version), LanguageUtils.getPersianNumbers(getVersion(getContext()))));
    }
}
