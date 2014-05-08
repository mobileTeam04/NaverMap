package kr.ac.kumoh.ce;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapLocationManager.OnLocationChangeListener;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnStateChangeListener;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;













import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;
import android.provider.Settings;

public class MainActivity extends NMapActivity {
	private static final String LOG_TAG = "NMapViewer";
	private static final boolean DEBUG = false;
	// set your API key which is registered for NMapViewer library.
	private static final String API_KEY = "d2716ca2113b5acf6b35c2507a328e1f";
	private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.978371, 37.5666091);
	private static final int NMAP_ZOOMLEVEL_DEFAULT = 11;
	private static final int NMAP_VIEW_MODE_DEFAULT = NMapView.VIEW_MODE_VECTOR;
	private static final boolean NMAP_TRAFFIC_MODE_DEFAULT = false;
	private static final boolean NMAP_BICYCLE_MODE_DEFAULT = false;
	private static final String KEY_ZOOM_LEVEL = "NMapViewer.zoomLevel";
	private static final String KEY_CENTER_LONGITUDE = "NMapViewer.centerLongitudeE6";
	private static final String KEY_CENTER_LATITUDE = "NMapViewer.centerLatitudeE6";
	private static final String KEY_VIEW_MODE = "NMapViewer.viewMode";
	private static final String KEY_TRAFFIC_MODE = "NMapViewer.trafficMode";
	private static final String KEY_BICYCLE_MODE = "NMapViewer.bicycleMode";
	
	NMapView mMapView;		//MapView ��ü(���� ����, ����������)
	NMapController mMapController;	//���� ���� ��Ʈ�� ��ü
//	OnMapStateChangeListener onMapViewStateChangeListener;	//�������� ���� �߻� �̺�Ʈ ó�� ��ü
	OnMapViewTouchEventListener onMapViewTouchEventListener; //��ġ �߻� �̺�Ʈ ó�� ��ü

	NMapViewerResourceProvider mMapViewerResourceProvider;	//���� ��� ���ҽ� ������ ��ü ����
	NMapOverlayManager mOverlayManager;		//�������� ���� ��ü
	OnStateChangeListener onPOIdataStateChangeListener;	//�������� ������ ��ȭ �̺�Ʈ ó�� ��ü

	NMapMyLocationOverlay mMyLocationOverlay;	//���� ���� ���� ��ġ�� ǥ���ϴ� �������� Ŭ����
	NMapLocationManager mMapLocationManager;	//�ܸ����� ���� ��ġ Ž�� ��� ��� Ŭ����
	MapContainerView mMapContainerView;
	NMapCompassManager mMapCompassManager;		//�ܸ����� ��ħ�� ��� ��� Ŭ����
	SharedPreferences mPreferences;
	
//	NMapCompassManager mMapCompassManager;
//	NMapMyLocationOverlay mMyLocationOverlay;
//	OnLocationChangeListener onMyLocationChangeListener;

	
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
        
        mMapLocationManager = new NMapLocationManager(this);
        
        //���� ��ġ ���� �� ȣ��Ǵ� �ݹ� �������̽��� �����Ѵ�.
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
        
        //(���� ��ġ, ��ħ�� ������)�� ���ڷ� ���� NMapMyLocationOverlay ��ü ���� ��ħ�� �����ڸ� null�� ����->���� ��ġ�� ǥ��
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
        
        //���� ��ġ ã�� �Լ� ȣ��
        startMyLocation();
/*      
        int markerId = NMapPOIflagType.PIN;

	     // set POI data
	     NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
	     poiData.beginPOIdata(2);
//	     poiData.addPOIitem(127.0630205, 37.5091300, "Pizza 777-111", markerId, 0);
//	     poiData.addPOIitem(127.061, 37.51, "Pizza 123-456", markerId, 0);
	     
	     poiData.addPOIitem(127.108099, 37.366034, "begin", NMapPOIflagType.FROM, 0);
   		 poiData.addPOIitem(127.106279, 37.366380, "end", NMapPOIflagType.TO, 0);

	     poiData.endPOIdata();
	
	     // create POI data overlay
	     NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
	     
	     poiDataOverlay.showAllPOIdata(0);
	     poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
	     
	     NMapPathData pathData = new NMapPathData(9);

	     pathData.initPathData();
	     pathData.addPathPoint(127.108099, 37.366034, NMapPathLineStyle.TYPE_SOLID);
	     pathData.addPathPoint(127.108088, 37.366043, 0);
	     pathData.addPathPoint(127.108079, 37.365619, 0);
	     pathData.addPathPoint(127.107458, 37.365608, 0);
	     pathData.addPathPoint(127.107232, 37.365608, 0);
	     pathData.addPathPoint(127.106904, 37.365624, 0);
	     pathData.addPathPoint(127.105933, 37.365621, NMapPathLineStyle.TYPE_DASH);
	     pathData.addPathPoint(127.105929, 37.366378, 0);
	     pathData.addPathPoint(127.106279, 37.366380, 0);
	     pathData.endPathData();

	     NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
*/
	}
	private void startMyLocation() {//�� ��ġ ã�Ƽ� �̵�.
		if (mMyLocationOverlay != null) {
			if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
				mOverlayManager.addOverlay(mMyLocationOverlay);
			}
			if (mMapLocationManager.isMyLocationEnabled()) {
				if (!mMapView.isAutoRotateEnabled()) {
					mMyLocationOverlay.setCompassHeadingVisible(true);
					mMapCompassManager.enableCompass();
					mMapView.setAutoRotateEnabled(true, false);
				} else {
					stopMyLocation();
				}
				mMapView.postInvalidate();
				
			} else {
				boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
				if (!isMyLocationEnabled) {
					Toast.makeText(MainActivity.this, "Please enable a My Location source in system settings",
						Toast.LENGTH_LONG).show();
					Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(goToSettings);
					return;
				}
			}
		}
	}


	private void stopMyLocation() {
		if (mMyLocationOverlay != null) {
			mMapLocationManager.disableMyLocation();
			if (mMapView.isAutoRotateEnabled()) {
				mMyLocationOverlay.setCompassHeadingVisible(false);
				mMapCompassManager.disableCompass();
				mMapView.setAutoRotateEnabled(false, false);
				mMapContainerView.requestLayout();
			}
		}
	}
	
	/* MyLocation Listener */

	private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {
		@Override
		public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
			if (mMapController != null) {
				mMapController.animateTo(myLocation);
			}
			return true;
		}

		@Override
		public void onLocationUpdateTimeout(NMapLocationManager locationManager) {
			Toast.makeText(MainActivity.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {
			Toast.makeText(MainActivity.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();
			stopMyLocation();
		}
	};

	/* MapView State Change Listener*/

	private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {
		@Override
		public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {
			if (errorInfo == null) { // success
				// restore map view state such as map center position and zoom level.
				restoreInstanceState();
			} else { // fail
				Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());
				Toast.makeText(MainActivity.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
			}

		}

		@Override
		public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
			}
		}

		@Override
		public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
			}
		}

		@Override
		public void onZoomLevelChange(NMapView mapView, int level) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
			}
		}

		@Override
		public void onMapCenterChangeFine(NMapView mapView) {
		}

	};
/////////////////////////////////////////////////////////////
	/* Local Functions */

	private void restoreInstanceState() {//������ ������ �о�ͼ� ����

		mPreferences = getPreferences(MODE_PRIVATE);
		int longitudeE6 = mPreferences.getInt(KEY_CENTER_LONGITUDE, NMAP_LOCATION_DEFAULT.getLongitudeE6());
		int latitudeE6 = mPreferences.getInt(KEY_CENTER_LATITUDE, NMAP_LOCATION_DEFAULT.getLatitudeE6());
		int level = mPreferences.getInt(KEY_ZOOM_LEVEL, NMAP_ZOOMLEVEL_DEFAULT);

		mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6), level);//�Էµ� ���浵+�ܷ����� ���� ȭ�� ����� �̵�.
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
	        	 Log.i("1234", "onFocusChanged: " + item.toString());
	        } else {
	        	 Log.i("1234", "onFocusChanged: ");
	        }
	}

	public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {
        // set your callout overlay
        return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
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
