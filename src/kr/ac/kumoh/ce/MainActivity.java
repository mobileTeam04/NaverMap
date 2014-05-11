package kr.ac.kumoh.ce;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnStateChangeListener;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;
import android.provider.Settings;

public class MainActivity extends NMapActivity {
	NMapView mMapView;		//MapView 객체(지도 생성, 지도데이터)
	NMapController mMapController;	//지도 상태 컨트롤 객체
	NMapViewerResourceProvider mMapViewerResourceProvider;	//지도 뷰어 리소스 곱급자 객체 생성
	NMapOverlayManager mOverlayManager;		//오버레이 관리 객체
	OnStateChangeListener onPOIdataStateChangeListener;		//오버레이 아이템 변화 이벤트 콜백 인터페이스
	NMapMyLocationOverlay mMyLocationOverlay;	//지도 위에 현재 위치를 표시하는 오버레이 클래스
	NMapLocationManager mMapLocationManager;	//단말기의 현재 위치 탐색 기능 사용 클래스
	NMapCompassManager mMapCompassManager;		//단말기의 나침반 기능 사용 클래스
	OnMapViewTouchEventListener onMapViewTouchEventListener;
	OnMapStateChangeListener onMapViewStateChangeListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//지도 화면 생성
		mMapView = new NMapView(this);		
		//API 키 설정
		mMapView.setApiKey("44cfbc1908a24ed56d0fdb6d9fc1c4b8");		
		//지도 화면 초기화
		mMapView.setClickable(true);
		//지도 상태 변화를 위한 listener 등록
		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
		//지도 컨트롤러(줌 인/아웃 등) 사용
		mMapController = mMapView.getMapController();
		// 줌 인/아웃 버튼 생성
        mMapView.setBuiltInZoomControls(true, null);
        //지도 중심좌표 및 축적 레벨 설정
        mMapController.setMapCenter(128.3925046, 36.1454420, 11);
        //맵뷰 모드 설정
 /*
        mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);	//일반지도
        mMapController.setMapViewMode(NMapView.VIEW_MODE_HYBRID);	//위성지도
        mMapController.setMapViewTrafficMode(true);	//실시간 교통지도 보기 모드 설정
        mMapController.setMapViewBicycleMode(true);	//자전거 지도 보기 모드 설정
 */       
        //화면에 지도 표시
        setContentView(mMapView);
        
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        mMapLocationManager = new NMapLocationManager(this);
        //현재 위치 변경 시 호출되는 콜백 인터페이스를 설정한다.
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
        //NMapMyLocationOverlay 객체 생성
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
        
//		startMyLocation();	//내 위치 찾기 시작 함수
//		testOverlayMaker();	//오버레이 아이템 생성
//		testOverlayPath();	//경로 표시 함수
	}
	
	private void startMyLocation() {//내 위치 찾아서 이동.
		if (mMapLocationManager.isMyLocationEnabled()) {	//현재 위치를 탐색중인지 확인
			if (!mMapView.isAutoRotateEnabled()) {			//지도 회전기능 활성화 상태 여부 확인
				mMyLocationOverlay.setCompassHeadingVisible(true);	//나침반 각도 표시
				mMapCompassManager.enableCompass();			//나침반 모니터링 시작
				mMapView.setAutoRotateEnabled(true, false);	//지도 회전기능 활성화(활성화 여부, 해제시 애니메이션 사용)
			}
			mMapView.invalidate();
			
		} else {
			boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(false);
			if (!isMyLocationEnabled) {	//위치 탐색이 불가능하면
				Toast.makeText(MainActivity.this, "Please enable a My Location source in system settings",
					Toast.LENGTH_LONG).show();
				Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(goToSettings);
				return;
			}
		}
	}

	private void stopMyLocation() {
		mMapLocationManager.disableMyLocation();	//현재 위치 탐색 종료
		if (mMapView.isAutoRotateEnabled()) {		//지도 회전기능이 활성화 상태라면
			mMyLocationOverlay.setCompassHeadingVisible(false);	//나침반 각도표시 제거
			mMapCompassManager.disableCompass();	//나침반 모니터링 종료
			mMapView.setAutoRotateEnabled(false, false);//지도 회전기능 중지
		}
	}
	
	private void testOverlayMaker() {
		int markerId = NMapPOIflagType.PIN;	//마커 id설정
		//POI 데이터 관리 클래스 생성(POI데이터 수, 사용 리소스 공급자)
		NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
		poiData.beginPOIdata(2);	// POI 아이템 추가 시작 
		poiData.addPOIitem(127.0630205, 37.5091300, "Pizza 777-111", markerId, 0);
		poiData.addPOIitem(127.061, 37.51, "Pizza 123-456", markerId, 0);
		poiData.endPOIdata();		// POI 아이템 추가 종료
		//POI 데이터 오버레이 객체 생성(여러 개의 오버레이 아이템을 포함할 수 있는 오버레이 클래스)
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		poiDataOverlay.showAllPOIdata(11);	//모든 POI 데이터를 화면에 표시(zomLevel)
		//POI 아이템이 선택 상태 변경 시 호출되는 콜백 인터페이스 설정
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
	}
	
	private void testOverlayPath() {
		//지도 위에 표시되는 POI 데이터 관리 클래스 생성
		NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
		poiData.beginPOIdata(2);
		poiData.addPOIitem(127.108099, 37.366034, "begin", NMapPOIflagType.FROM, 0);
		poiData.addPOIitem(127.106279, 37.366380, "end", NMapPOIflagType.TO, 0);
		poiData.endPOIdata();
	
		//POI 데이터 오버레이 객체 생성(여러 개의 오버레이 아이템을 포함할 수 있는 오버레이 클래스)
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);	     
		poiDataOverlay.showAllPOIdata(11);	//모든 POI 데이터를 화면에 표시(zomLevel)
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);	//콜백 인터페이스 설정
		
		//경로 관리 클래스 생성(경로 데이터의 개수)
		NMapPathData pathData = new NMapPathData(9);
		pathData.initPathData();	//경로 데이터 추가 시작
		//경로 데이터의 보간점 좌표 추가 - 좌표, 선 Type 설정(0으로 할시 이전 값 그대로 사용)
		pathData.addPathPoint(127.108099, 37.366034, NMapPathLineStyle.TYPE_SOLID);
		pathData.addPathPoint(127.108088, 37.366043, 0);
		pathData.addPathPoint(127.108079, 37.365619, 0);
		pathData.addPathPoint(127.107458, 37.365608, 0);
		pathData.addPathPoint(127.107232, 37.365608, 0);
		pathData.addPathPoint(127.106904, 37.365624, 0);
		pathData.addPathPoint(127.105933, 37.365621, NMapPathLineStyle.TYPE_DASH);
		pathData.addPathPoint(127.105929, 37.366378, 0);
		pathData.addPathPoint(127.106279, 37.366380, 0);
		pathData.endPathData();	//경로 데이터 추가 종료
		//경로 데이터를 표시 오버레이 객체 생성
		NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
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
		}

		@Override
		public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {
			stopMyLocation();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.reset) {
			if (mMyLocationOverlay != null) {
				stopMyLocation();
				mOverlayManager.removeOverlay(mMyLocationOverlay);	//나침반 오버레이 객체 제거
			}
			mOverlayManager.clearOverlays();	//NMapMyLocationOverlay 객체를 제외한 모은 오버레이 객체 제거
			return true;
		}
		if (id == R.id.locate_find) {
			startMyLocation();
			return true;
		}
		if (id == R.id.overlay_item_set) {
			testOverlayMaker();
			return true;
		}
		if (id == R.id.draw_line) {
			testOverlayPath();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
