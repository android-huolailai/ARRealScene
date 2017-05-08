package com.demo.arrealscene.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;


/** 
 * @ClassName: CameraView 
 * @Description: ������󶨵�SurfaceView ��װ�����շ���
 * @author LinJ
 * @date 2014-12-31 ����9:44:56 
 *  
 */
public class CameraView extends SurfaceView{

	public final static String TAG="CameraView";
	/** �͸�View�󶨵�Camera���� */
	private Camera mCamera;

	/** ��ǰ��������ͣ�Ĭ��Ϊ�ر� */ 
	private FlashMode mFlashMode= FlashMode.ON;

	/** ��ǰ���ż���  Ĭ��Ϊ0*/ 
	private int mZoom=0;

	/** ��ǰ��Ļ��ת�Ƕ�*/ 
	private int mOrientation=0;
	/** �Ƿ��ǰ�����,trueΪǰ��,falseΪ����  */ 
	private boolean mIsFrontCamera;
	/**  ¼���� */ 
	private MediaRecorder mMediaRecorder;
	/**  ������ã���¼��ǰ��¼������¼�������ָ�ԭ���� */ 
	private Parameters mParameters;
	/**  ¼����·�� ��������������ͼ*/
	private String mRecordPath=null;
	public CameraView(Context context){
		super(context);
		//��ʼ������
		getHolder().addCallback(callback);

		openCamera();

		mIsFrontCamera=false;
	}
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//��ʼ������
		getHolder().addCallback(callback);

		openCamera();

		mIsFrontCamera=false;
	}
	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if(mCamera==null){
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
			mCamera.startPreview();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			updateCameraOrientation();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}
	};
	/**
	 *   ���ݵ�ǰ�����״̬(ǰ�û����)���򿪶�Ӧ���
	 */
	public boolean openCamera()  {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if(mIsFrontCamera){
			CameraInfo cameraInfo=new CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if(cameraInfo.facing== CameraInfo.CAMERA_FACING_FRONT){
					try {
						mCamera=Camera.open(i);
					} catch (Exception e) {
						mCamera =null;
						return false;
					}
				}
			}
		}else {
			try {
				mCamera=Camera.open();
			} catch (Exception e) {
				mCamera =null;
				return false;
			}
		}
		return true;
	}

	/**
	 * 重新预览
	 */
	public void startPreview(){
		mCamera.startPreview();
	}
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	public void setFlashMode(FlashMode flashMode) {
		if(mCamera==null) return;
		mFlashMode = flashMode;
		Parameters parameters=mCamera.getParameters();
		switch (flashMode) {
		case ON:
			parameters.setFlashMode(Parameters.FLASH_MODE_ON);
			break;
		case AUTO:
			parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			break;
		case TORCH:
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			break;
		default:
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			break;
		}
		mCamera.setParameters(parameters);
	}
	public void takePicture(PictureCallback callback){
		mCamera.takePicture(null, null, callback);
	}
	public void setZoom(int zoom){
		if(mCamera==null) return;
		Parameters parameters;
		//ע��˴�Ϊ¼��ģʽ�µ�setZoom��ʽ����Camera.unlock֮�󣬵���getParameters����������android��ܵײ���쳣
		//stackoverflow�Ͽ����Ľ��������ڶ��߳�ͬʱ����Camera���µĳ�ͻ�������ڴ�ʹ��¼��ǰ�����mParameters��
		if(mParameters!=null)
			parameters=mParameters;
		else {
			parameters=mCamera.getParameters();
		}

		if(!parameters.isZoomSupported()) return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom=zoom;
	}
	/**
	 * �������������
	 */
	private void setCameraParameters(){
		Parameters parameters = mCamera.getParameters();
		// ѡ����ʵ�Ԥ���ߴ�
		List<Size> sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			//Ԥ��ͼƬ��С
//			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
			parameters.setPreviewSize(640, 480);
		}

		//�������ɵ�ͼƬ��С
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			for (Size size : sizeList) {
				//С��100W����
				if (size.width*size.height<100*10000) {
					cameraSize=size;
					break;
				}
			}
//			parameters.setPictureSize(cameraSize.width, cameraSize.height);
			parameters.setPictureSize(640, 480);
		}
		//����ͼƬ��ʽ
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		//�Զ��۽�ģʽ
		parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		//���������ģʽ���˴���Ҫ������������ݻٺ����ؽ�������֮ǰ��״̬
		setFlashMode(mFlashMode);
		//�������ż���
		setZoom(mZoom);
		//������Ļ�������
		startOrientationChangeListener();
	}
	/**
	 *   ������Ļ����ı�������� ��������Ļ�������л�ʱ�ı䱣���ͼƬ�ķ���
	 */
	private  void startOrientationChangeListener() {
		OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {
			@Override
			public void onOrientationChanged(int rotation) {

				if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)){
					rotation=0;
				}else if ((rotation > 45) && (rotation <= 135))  {
					rotation=90;
				}
				else if ((rotation > 135) && (rotation <= 225)) {
					rotation=180;
				}
				else if((rotation > 225) && (rotation <= 315)) {
					rotation=270;
				}else {
					rotation=0;
				}
				if(rotation==mOrientation)
					return;
				mOrientation=rotation;
				updateCameraOrientation();
			}
		};
		mOrEventListener.enable();
	}
	/**
	 *   ���ݵ�ǰ�����޸ı���ͼƬ����ת�Ƕ�
	 */
	private void updateCameraOrientation(){
		if(mCamera!=null){
			Parameters parameters = mCamera.getParameters();
			//rotation����Ϊ 0��90��180��270��ˮƽ����Ϊ0��
			int rotation=90+mOrientation==360?0:90+mOrientation;
			//ǰ������ͷ��Ҫ�Դ�ֱ�������任��������Ƭ�ǵߵ���
			if(mIsFrontCamera){
				if(rotation==90) rotation=270;
				else if (rotation==270) rotation=90;
			}
			parameters.setRotation(rotation);//���ɵ�ͼƬת90��
			//Ԥ��ͼƬ��ת90��
			mCamera.setDisplayOrientation(90);//Ԥ��ת90��
			//设置对焦模式
			parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			mCamera.setParameters(parameters);
			mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
		}
	}
	/** 
	 * @Description: ���������ö�� Ĭ��Ϊ�ر�
	 */
	public enum FlashMode{
		/** ON:����ʱ�������   */ 
		ON,
		/** OFF�����������  */ 
		OFF,
		/** AUTO��ϵͳ�����Ƿ�������  */ 
		AUTO,
		/** TORCH��һֱ�������  */ 
		TORCH
	}
}