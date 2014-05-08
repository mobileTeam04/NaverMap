package kr.ac.kumoh.ce;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.nmapmodel.NMapError;

import com.nhn.android.mapviewer.overlay.NMapResourceProvider;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;


import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends NMapActivity {
	NMapView mMapView;		//MapView ��ü(���� ����, ����������)
	NMapController mMapController;	//���� ���� ��Ʈ�� ��ü
	OnMapStateChangeListener onMapViewStateChangeListener;	//�������� ���� �߻� �̺�Ʈ ó��
	OnMapViewTouchEventListener onMapViewTouchEventListener; //��ġ �߻� �̺�Ʈ ó��

	
	
	
	NMapResourceProvider mMapViewerResourceProvider;
	NMapOverlayManager mOverlayManager;
	NMapLocationManager mMapLocationManager;
	NMapCompassManager mMapCompassManager;
	NMapMyLocationOverlay mMyLocationOverlay;
	OnLocationChangeListener onMyLocationChangeListener;
	MapContainerView mMapContainerView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//���� ȭ�� ����
		mMapView = new NMapView(this);
		
		//API Ű ����
		mMapView.setApiKey("44cfbc1908a24ed56d0fdb6d9fc1c4b8");
		
		//���� ȭ�� �ʱ�ȭ
		mMapView.setClickable(true);
		
		//���� ���� ��ȭ�� ���� listener ���
		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
		
		//���� ��Ʈ�ѷ�(�� ��/�ƿ� ��) ���
		mMapController = mMapView.getMapController();
		
		// �� ��/�ƿ� ��ư ����
        mMapView.setBuiltInZoomControls(true, null);
        
        //ȭ�鿡 ���� ǥ��
        setContentView(mMapView);
        
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
        
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        
        
        int markerId = NMapPOIflagType.PIN;

	     // set POI data
	     NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
	     poiData.beginPOIdata(2);
	     poiData.addPOIitem(127.0630205, 37.5091300, "Pizza 777-111", markerId, 0);
	     poiData.addPOIitem(127.061, 37.51, "Pizza 123-456", markerId, 0);
	     poiData.endPOIdata();
	
	     // create POI data overlay
	     NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
	     
	     poiDataOverlay.showAllPOIdata(0);
	     
	     mMapLocationManager = new NMapLocationManager(this);
	     mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

	     // compass manager
	     mMapCompassManager = new NMapCompassManager(this);

	     // create my location overlay
	     mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
	     setContentView(mMapView);
//	     setContentView(mMapContainerView);
	}
	
	public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {
        if (errorInfo == null) { // success
                mMapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 11);
        } else { // fail
                Log.e("MAP", "onMapInitHandler: error=" + errorInfo.toString());
        }
	} 

	public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
        // [[TEMP]] handle a click event of the callout
	        Toast.makeText(MainActivity.this, "onCalloutClick: " + item.getTitle(), Toast.LENGTH_LONG).show();
	}

	public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
	        if (item != null) {
	        	 Log.i("MAP", "onFocusChanged: " + item.toString());
	        } else {
	        	 Log.i("MAP", "onFocusChanged: ");
	        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	private class MapContainerView extends ViewGroup {

		public MapContainerView(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			final int width = getWidth();
			final int height = getHeight();
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);
				final int childWidth = view.getMeasuredWidth();
				final int childHeight = view.getMeasuredHeight();
				final int childLeft = (width - childWidth) / 2;
				final int childTop = (height - childHeight) / 2;
				view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}

			if (changed) {
				mOverlayManager.onSizeChanged(width, height);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			int sizeSpecWidth = widthMeasureSpec;
			int sizeSpecHeight = heightMeasureSpec;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);

				if (view instanceof NMapView) {
					if (mMapView.isAutoRotateEnabled()) {
						int diag = (((int)(Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
						sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
						sizeSpecHeight = sizeSpecWidth;
					}
				}

				view.measure(sizeSpecWidth, sizeSpecHeight);
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
