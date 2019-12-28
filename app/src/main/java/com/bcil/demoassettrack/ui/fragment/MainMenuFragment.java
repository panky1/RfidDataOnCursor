package com.bcil.demoassettrack.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.service.BackgroundService;
import com.bcil.demoassettrack.ui.activity.LoginActivity;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.utils.MyKeyboard;
import com.bcil.demoassettrack.utils.PreferenceManager;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainMenuFragment extends Fragment {

    private PreferenceManager preferenceManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ButterKnife.bind(this, view);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        setHasOptionsMenu(true);
        initView();

        return view;
    }


    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("MainMenu");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.cvMapping, R.id.cvScanAst, R.id.cvAstScrap, R.id.cvAstSold,R.id.cvAstAlloc, R.id.cvLocTrans})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cvMapping:
                MappingFragment mappingFragment = new MappingFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container,mappingFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.cvScanAst:
                ScanAssetFragment scanAssetFragment = new ScanAssetFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction4
                        = getFragmentManager().beginTransaction();
                fragmentTransaction4.replace(R.id.main_container,scanAssetFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction4.addToBackStack(null);
                fragmentTransaction4.commit();
                break;
            case R.id.cvAstScrap:
                AssetScrapFragment assetScrapFragment = new AssetScrapFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction1
                        = getFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.main_container,assetScrapFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction1.addToBackStack(null);
                fragmentTransaction1.commit();
                break;
            case R.id.cvAstSold:
                AssetSoldFragment assetSoldFragment = new AssetSoldFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction2
                        = getFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.main_container,assetSoldFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.commit();
                break;
            case R.id.cvAstAlloc:
                AssetAllocateFragment assetAllocateFragment = new AssetAllocateFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction3
                        = getFragmentManager().beginTransaction();
                fragmentTransaction3.replace(R.id.main_container,assetAllocateFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction3.addToBackStack(null);
                fragmentTransaction3.commit();
                break;
            case R.id.cvLocTrans:
                LocTransferFragment locTransferFragment = new LocTransferFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction5
                        = getFragmentManager().beginTransaction();
                fragmentTransaction5.replace(R.id.main_container,locTransferFragment,MainActivity.TAG_CONTENT_FRAGMENT);
                fragmentTransaction5.addToBackStack(null);
                fragmentTransaction5.commit();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to exit")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                getActivity().finish();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });
                android.support.v7.app.AlertDialog alert = builder.create();
                alert.show();

                return true;
            case R.id.action_setting:
                SettingListFragment settingsFragment = new SettingListFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction5
                        = getFragmentManager().beginTransaction();
                fragmentTransaction5.replace(R.id.main_container,settingsFragment);
                fragmentTransaction5.addToBackStack(null);
                fragmentTransaction5.commit();
                return true;
            case R.id.action_logout:
                preferenceManager.clearSharedPreferance();
                Intent intent = new Intent(getActivity(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
