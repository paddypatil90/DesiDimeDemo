package com.paddy.desi.dime.app.fragements;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.paddy.desi.dime.app.R;
import com.paddy.desi.dime.app.activities.MainActivity;
import com.paddy.desi.dime.app.adapters.CustomGridAdapter;
import com.paddy.desi.dime.app.adapters.DealsCustomListAdapter;
import com.paddy.desi.dime.app.models.MainProductsSingletonModel;
import com.paddy.desi.dime.app.utils.AlertDialogManager;
import com.paddy.desi.dime.app.utils.AsyncReuse;
import com.paddy.desi.dime.app.utils.Constants;
import com.paddy.desi.dime.app.utils.GetResponse;
import com.paddy.desi.dime.app.utils.InternetConnectionDetector;
import org.apache.http.NameValuePair;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class PopularFragment extends Fragment implements GetResponse {
    // declare global variable
    public static final String TAG = PopularFragment.class
            .getSimpleName();
    DealsCustomListAdapter adapter;
    RecyclerView rec_view;
    InternetConnectionDetector icd;
    AsyncReuse asyncReuse;
    GridView gridview;
    CustomGridAdapter custom_gridadpter;
    Boolean LayoutFlag = true;
    public PopularFragment() {
        // Required empty public constructor
    }
    public static PopularFragment newInstance() {
        return new PopularFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_popular, container, false);
        // find recylcler view reference from xml
        gridview = (GridView) view.findViewById(R.id.popular_grid_view);
        rec_view = (RecyclerView) view.findViewById(R.id.PopularListview);
        rec_view.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // set layout parameter to recylcer view
        rec_view.setLayoutManager(layoutManager);
        // create object of InternetConnectionDetector
        icd = new InternetConnectionDetector(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // check if pupular list is null or not
        if(MainProductsSingletonModel.getInstance().getPopularDealsList()!= null){
            //  set adapter to recyclerview
                updateAdapter();
                updateGridAdapter();
        }else {
            //  check internet connection is available or not
            if(icd.isConnectingToInternet()){
                ExecuteServerRequest();
            }else{
                //  display Error message
                networkErrorMsg();
            }
        }
    }
    public void updateLayout() {
        if (LayoutFlag) {
            gridview.setVisibility(View.VISIBLE);
            rec_view.setVisibility(View.GONE);
            LayoutFlag = false;
            ((MainActivity)getActivity()).updateGridImage(true);
        } else {
            gridview.setVisibility(View.GONE);
            rec_view.setVisibility(View.VISIBLE);
            LayoutFlag = true;
            ((MainActivity)getActivity()).updateGridImage(false);
        }

    }

    public void updateAdapter(){
        //  create object of DealsCustomAdapter pass this context and popular list as parameter
        adapter = new DealsCustomListAdapter(getActivity(),MainProductsSingletonModel.getInstance().getPopularDealsList());
        //  set adapter to recycler view
        rec_view.setAdapter(adapter);
    }
    public void updateGridAdapter() {
        //  create object of CustomGridAdapter pass this context and Top list as parameter
        custom_gridadpter = new CustomGridAdapter(getActivity(), MainProductsSingletonModel.getInstance().getPopularDealsList());
        //  set adapter to recycler view
        gridview.setAdapter(custom_gridadpter);
    }

    //  networkErrorMsg method display error massage
    public void networkErrorMsg(){
            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getActivity().getResources().getString(R.string.oops))
                    .setContentText(getActivity().getResources().getString(R.string.you_dont_have_internt_conn))
                    .setCancelText(getActivity().getResources().getString(R.string.Cancel))
                    .setConfirmText(getActivity().getResources().getString(R.string.Try_again))
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(icd.isConnectingToInternet()){
                                        ExecuteServerRequest();
                                    }else{
                                        networkErrorMsg();
                                    }
                                }
                            });
                        }
                    })
                    .show();
        }

    //  execute server request
    public void ExecuteServerRequest(){
        List<NameValuePair> paramslist = null;
        //  create object of AsyncReuse pass this context, server url, Request type, param, and loader flag as parameter
        asyncReuse = new AsyncReuse(getActivity(), Constants.APP_MAINURL + "popular.json", Constants.GET, paramslist, false);
        //  passing this reference to callback interface
        asyncReuse.getResponse = this;
        //  execute async task
        asyncReuse.execute();
    }

    @Override
    public void GetData(String response) {
        if (response != null){
            // Parse server response
            MainProductsSingletonModel.getInstance().GetPopularDealsDataFromServer(response);
            // check popular list size
            if (MainProductsSingletonModel.getInstance().getPopularDealsList().size() > 0) {
                // update listview
                updateAdapter();
                updateGridAdapter();
            } else {
                // show error message
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       networkErrorMsg();
                    }
                });
            }
        }
    }
}
