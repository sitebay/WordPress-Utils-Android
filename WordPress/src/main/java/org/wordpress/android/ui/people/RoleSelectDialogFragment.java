package org.sitebay.android.ui.people;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.sitebay.android.R;
import org.sitebay.android.WordPress;
import org.sitebay.android.fluxc.model.RoleModel;
import org.sitebay.android.fluxc.model.SiteModel;
import org.sitebay.android.fluxc.store.SiteStore;
import org.sitebay.android.models.RoleUtils;
import org.sitebay.android.viewmodel.ContextProvider;

import java.util.List;

import javax.inject.Inject;

public class RoleSelectDialogFragment extends DialogFragment {
    @Inject SiteStore mSiteStore;
    @Inject ContextProvider mContextProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getActivity().getApplicationContext()).component().inject(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SiteModel site = (SiteModel) getArguments().getSerializable(WordPress.SITE);
        final List<RoleModel> inviteRoles = RoleUtils.getInviteRoles(mSiteStore, site, mContextProvider.getContext());
        final String[] stringRoles = new String[inviteRoles.size()];
        for (int i = 0; i < inviteRoles.size(); i++) {
            stringRoles[i] = inviteRoles.get(i).getDisplayName();
        }

        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.role);
        builder.setItems(stringRoles, (dialog, which) -> {
            if (!isAdded()) {
                return;
            }

            if (getTargetFragment() instanceof OnRoleSelectListener) {
                ((OnRoleSelectListener) getTargetFragment()).onRoleSelected(inviteRoles.get(which));
            } else if (getActivity() instanceof OnRoleSelectListener) {
                ((OnRoleSelectListener) getActivity()).onRoleSelected(inviteRoles.get(which));
            }
        });

        return builder.create();
    }

    public static <T extends Fragment & OnRoleSelectListener> void show(T parentFragment, int requestCode,
                                                                        @NonNull SiteModel site) {
        RoleSelectDialogFragment roleChangeDialogFragment = new RoleSelectDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(WordPress.SITE, site);
        roleChangeDialogFragment.setArguments(args);
        roleChangeDialogFragment.setTargetFragment(parentFragment, requestCode);
        roleChangeDialogFragment.show(parentFragment.getFragmentManager(), null);
    }

    public static <T extends AppCompatActivity & OnRoleSelectListener> void show(T parentActivity) {
        RoleSelectDialogFragment roleChangeDialogFragment = new RoleSelectDialogFragment();
        roleChangeDialogFragment.show(parentActivity.getSupportFragmentManager(), null);
    }

    // Container Activity must implement this interface
    interface OnRoleSelectListener {
        void onRoleSelected(RoleModel newRole);
    }
}
