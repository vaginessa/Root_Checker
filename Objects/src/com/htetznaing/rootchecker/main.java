package com.htetznaing.rootchecker;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "com.htetznaing.rootchecker", "com.htetznaing.rootchecker.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "com.htetznaing.rootchecker", "com.htetznaing.rootchecker.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "com.htetznaing.rootchecker.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        Object[] o;
        if (permissions.length > 0)
            o = new Object[] {permissions[0], grantResults[0] == 0};
        else
            o = new Object[] {"", false};
        processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.Timer _t1 = null;
public static anywheresoftware.b4a.objects.Timer _t2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _about = null;
public MLfiles.Fileslib.MLfiles _ml = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b = null;
public anywheresoftware.b4a.phone.Phone _p = null;
public static String _os = "";
public anywheresoftware.b4a.objects.ImageViewWrapper _ivno = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _ivyes = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imv = null;
public anywheresoftware.b4a.objects.LabelWrapper _yes = null;
public anywheresoftware.b4a.objects.LabelWrapper _no = null;
public anywheresoftware.b4a.admobwrapper.AdViewWrapper _banner = null;
public mobi.mindware.admob.interstitial.AdmobInterstitialsAds _interstitial = null;
public anywheresoftware.b4a.objects.LabelWrapper _lf = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
anywheresoftware.b4a.objects.drawable.ColorDrawable _bbg = null;
 //BA.debugLineNum = 36;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 37;BA.debugLine="If p.SdkVersion > 19 Then";
if (mostCurrent._p.getSdkVersion()>19) { 
 //BA.debugLineNum = 38;BA.debugLine="banner.Initialize(\"banner\",\"ca-app-pub-417334857";
mostCurrent._banner.Initialize(mostCurrent.activityBA,"banner","ca-app-pub-4173348573252986/6102038158");
 //BA.debugLineNum = 39;BA.debugLine="banner.LoadAd";
mostCurrent._banner.LoadAd();
 //BA.debugLineNum = 40;BA.debugLine="Activity.AddView(banner,0%x,100%y - 50dip,100%x,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._banner.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50))),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 42;BA.debugLine="interstitial.Initialize(\"interstitial\",\"ca-app-p";
mostCurrent._interstitial.Initialize(mostCurrent.activityBA,"interstitial","ca-app-pub-4173348573252986/1532237754");
 //BA.debugLineNum = 43;BA.debugLine="interstitial.LoadAd";
mostCurrent._interstitial.LoadAd(mostCurrent.activityBA);
 //BA.debugLineNum = 45;BA.debugLine="t1.Initialize(\"t1\",100)";
_t1.Initialize(processBA,"t1",(long) (100));
 //BA.debugLineNum = 46;BA.debugLine="t1.Enabled = False";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 48;BA.debugLine="t2.Initialize(\"t2\",30000)";
_t2.Initialize(processBA,"t2",(long) (30000));
 //BA.debugLineNum = 49;BA.debugLine="t2.Enabled = True";
_t2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 51;BA.debugLine="Activity.Color = Colors.White";
mostCurrent._activity.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 52;BA.debugLine="Select p.SdkVersion";
switch (BA.switchObjectToInt(mostCurrent._p.getSdkVersion(),(int) (2),(int) (3),(int) (4),(int) (5),(int) (6),(int) (7),(int) (8),(int) (9),(int) (10),(int) (11),(int) (12),(int) (13),(int) (14),(int) (15),(int) (16),(int) (17),(int) (18),(int) (19),(int) (21),(int) (22),(int) (23),(int) (24),(int) (25))) {
case 0: {
 //BA.debugLineNum = 53;BA.debugLine="Case 2 : OS = \"1.1\"";
mostCurrent._os = "1.1";
 break; }
case 1: {
 //BA.debugLineNum = 54;BA.debugLine="Case 3 : OS = \"1.5\"";
mostCurrent._os = "1.5";
 break; }
case 2: {
 //BA.debugLineNum = 55;BA.debugLine="Case 4 : OS = \"1.6\"";
mostCurrent._os = "1.6";
 break; }
case 3: {
 //BA.debugLineNum = 56;BA.debugLine="Case 5 : OS = \"2.0\"";
mostCurrent._os = "2.0";
 break; }
case 4: {
 //BA.debugLineNum = 57;BA.debugLine="Case 6 : OS = \"2.0.1\"";
mostCurrent._os = "2.0.1";
 break; }
case 5: {
 //BA.debugLineNum = 58;BA.debugLine="Case 7 : OS = \"2.1\"";
mostCurrent._os = "2.1";
 break; }
case 6: {
 //BA.debugLineNum = 59;BA.debugLine="Case 8 : OS = \"2.2\"";
mostCurrent._os = "2.2";
 break; }
case 7: {
 //BA.debugLineNum = 60;BA.debugLine="Case 9 : OS = \"2.3 - 2.3.2\"";
mostCurrent._os = "2.3 - 2.3.2";
 break; }
case 8: {
 //BA.debugLineNum = 61;BA.debugLine="Case 10 : OS = \"	2.3.3 - 2.3.7\" ' 2.3.3 or 2.3.";
mostCurrent._os = "	2.3.3 - 2.3.7";
 break; }
case 9: {
 //BA.debugLineNum = 62;BA.debugLine="Case 11 : OS = \"3.0\"";
mostCurrent._os = "3.0";
 break; }
case 10: {
 //BA.debugLineNum = 63;BA.debugLine="Case 12 : OS = \"3.1\"";
mostCurrent._os = "3.1";
 break; }
case 11: {
 //BA.debugLineNum = 64;BA.debugLine="Case 13 : OS = \"3.2\"";
mostCurrent._os = "3.2";
 break; }
case 12: {
 //BA.debugLineNum = 65;BA.debugLine="Case 14 : OS = \"	4.0.1 - 4.0.2\"";
mostCurrent._os = "	4.0.1 - 4.0.2";
 break; }
case 13: {
 //BA.debugLineNum = 66;BA.debugLine="Case 15 : OS = \"4.0.3 - 4.0.4\"";
mostCurrent._os = "4.0.3 - 4.0.4";
 break; }
case 14: {
 //BA.debugLineNum = 67;BA.debugLine="Case 16 : OS = \"	4.1.x\"";
mostCurrent._os = "	4.1.x";
 break; }
case 15: {
 //BA.debugLineNum = 68;BA.debugLine="Case 17 : OS = \"	4.2.x\"";
mostCurrent._os = "	4.2.x";
 break; }
case 16: {
 //BA.debugLineNum = 69;BA.debugLine="Case 18 : OS = 	\"4.3.x\"";
mostCurrent._os = "4.3.x";
 break; }
case 17: {
 //BA.debugLineNum = 70;BA.debugLine="Case 19 : OS = \"	4.4 - 4.4.4\"";
mostCurrent._os = "	4.4 - 4.4.4";
 break; }
case 18: {
 //BA.debugLineNum = 71;BA.debugLine="Case 21: OS = \"5.0\"";
mostCurrent._os = "5.0";
 break; }
case 19: {
 //BA.debugLineNum = 72;BA.debugLine="Case 22: OS = \"5.1\"";
mostCurrent._os = "5.1";
 break; }
case 20: {
 //BA.debugLineNum = 73;BA.debugLine="Case 23: OS = \"6.0\"";
mostCurrent._os = "6.0";
 break; }
case 21: {
 //BA.debugLineNum = 74;BA.debugLine="Case 24 : OS = \"	7.0\"";
mostCurrent._os = "	7.0";
 break; }
case 22: {
 //BA.debugLineNum = 75;BA.debugLine="Case 25 : OS = \"	7.1\"";
mostCurrent._os = "	7.1";
 break; }
default: {
 //BA.debugLineNum = 76;BA.debugLine="Case Else : OS = \"?\"";
mostCurrent._os = "?";
 break; }
}
;
 //BA.debugLineNum = 79;BA.debugLine="imv.Initialize(\"imv\")";
mostCurrent._imv.Initialize(mostCurrent.activityBA,"imv");
 //BA.debugLineNum = 80;BA.debugLine="imv.Bitmap = LoadBitmap(File.DirAssets,\"icon.png\")";
mostCurrent._imv.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"icon.png").getObject()));
 //BA.debugLineNum = 81;BA.debugLine="imv.Gravity = Gravity.FILL";
mostCurrent._imv.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.FILL);
 //BA.debugLineNum = 82;BA.debugLine="Activity.AddView(imv,50%x - 50dip,10dip, 100dip, 1";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imv.getObject()),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 84;BA.debugLine="About.Initialize(\"\")";
mostCurrent._about.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 85;BA.debugLine="About.Text = \"Brand Name : \" & p.Manufacturer & CR";
mostCurrent._about.setText((Object)("Brand Name : "+mostCurrent._p.getManufacturer()+anywheresoftware.b4a.keywords.Common.CRLF+anywheresoftware.b4a.keywords.Common.CRLF+"Device Name : "+mostCurrent._p.getModel()+anywheresoftware.b4a.keywords.Common.CRLF+anywheresoftware.b4a.keywords.Common.CRLF+"Androide Version : "+mostCurrent._os));
 //BA.debugLineNum = 86;BA.debugLine="Activity.AddView(About,0%x,(imv.Height+imv.Top),10";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._about.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) ((mostCurrent._imv.getHeight()+mostCurrent._imv.getTop())),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (120)));
 //BA.debugLineNum = 87;BA.debugLine="About.Gravity = Gravity.CENTER";
mostCurrent._about.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 88;BA.debugLine="About.TextColor = Colors.Black";
mostCurrent._about.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 90;BA.debugLine="Dim bbg As ColorDrawable";
_bbg = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 91;BA.debugLine="bbg.Initialize(Colors.Black,10)";
_bbg.Initialize(anywheresoftware.b4a.keywords.Common.Colors.Black,(int) (10));
 //BA.debugLineNum = 92;BA.debugLine="b.Initialize(\"b\")";
mostCurrent._b.Initialize(mostCurrent.activityBA,"b");
 //BA.debugLineNum = 93;BA.debugLine="b.Background = bbg";
mostCurrent._b.setBackground((android.graphics.drawable.Drawable)(_bbg.getObject()));
 //BA.debugLineNum = 94;BA.debugLine="b.Text = \"#Verify Root Status\"";
mostCurrent._b.setText((Object)("#Verify Root Status"));
 //BA.debugLineNum = 95;BA.debugLine="Activity.AddView(b,20%x,(About.Top+About.Height),6";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._b.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),(int) ((mostCurrent._about.getTop()+mostCurrent._about.getHeight())),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 97;BA.debugLine="ivyes.Initialize(\"ivyes\")";
mostCurrent._ivyes.Initialize(mostCurrent.activityBA,"ivyes");
 //BA.debugLineNum = 98;BA.debugLine="ivyes.Bitmap = LoadBitmap(File.DirAssets,\"yes.png\"";
mostCurrent._ivyes.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"yes.png").getObject()));
 //BA.debugLineNum = 99;BA.debugLine="ivyes.Gravity = Gravity.FILL";
mostCurrent._ivyes.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.FILL);
 //BA.debugLineNum = 100;BA.debugLine="Activity.AddView(ivyes,50%x - 30dip  , (About.Top+";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._ivyes.getObject()),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30))),(int) ((mostCurrent._about.getTop()+mostCurrent._about.getHeight())),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)));
 //BA.debugLineNum = 101;BA.debugLine="ivyes.Visible = False";
mostCurrent._ivyes.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 103;BA.debugLine="ivno.Initialize(\"ivno\")";
mostCurrent._ivno.Initialize(mostCurrent.activityBA,"ivno");
 //BA.debugLineNum = 104;BA.debugLine="ivno.Bitmap = LoadBitmap(File.DirAssets,\"no.png\")";
mostCurrent._ivno.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"no.png").getObject()));
 //BA.debugLineNum = 105;BA.debugLine="ivno.Gravity = Gravity.FILL";
mostCurrent._ivno.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.FILL);
 //BA.debugLineNum = 106;BA.debugLine="Activity.AddView(ivno,50%x - 30dip , (About.Top+Ab";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._ivno.getObject()),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30))),(int) ((mostCurrent._about.getTop()+mostCurrent._about.getHeight())),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)));
 //BA.debugLineNum = 107;BA.debugLine="ivno.Visible = False";
mostCurrent._ivno.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 109;BA.debugLine="yes.Initialize(\"yes\")";
mostCurrent._yes.Initialize(mostCurrent.activityBA,"yes");
 //BA.debugLineNum = 110;BA.debugLine="yes.Text = \"Congratulations :)\" &CRLF& \"Root Acc";
mostCurrent._yes.setText((Object)("Congratulations :)"+anywheresoftware.b4a.keywords.Common.CRLF+"Root Access is Properly Installed on Your Device!"));
 //BA.debugLineNum = 111;BA.debugLine="yes.Gravity = Gravity.CENTER";
mostCurrent._yes.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 112;BA.debugLine="yes.TextColor = Colors.Green";
mostCurrent._yes.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Green);
 //BA.debugLineNum = 113;BA.debugLine="Activity.AddView(yes,0%x,(ivyes.Top+ivyes.Height";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._yes.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) ((mostCurrent._ivyes.getTop()+mostCurrent._ivyes.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (2),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 114;BA.debugLine="yes.Visible = False";
mostCurrent._yes.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 116;BA.debugLine="no.Initialize(\"no\")";
mostCurrent._no.Initialize(mostCurrent.activityBA,"no");
 //BA.debugLineNum = 117;BA.debugLine="no.Text =  \"Sorry :(\" &CRLF& \"Root Access is Not";
mostCurrent._no.setText((Object)("Sorry :("+anywheresoftware.b4a.keywords.Common.CRLF+"Root Access is Not Properly Installed on Your Device!"+anywheresoftware.b4a.keywords.Common.CRLF+"How to Root Your Device ?"));
 //BA.debugLineNum = 118;BA.debugLine="no.Gravity = Gravity.CENTER";
mostCurrent._no.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 119;BA.debugLine="no.TextColor = Colors.Red";
mostCurrent._no.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Red);
 //BA.debugLineNum = 120;BA.debugLine="Activity.AddView(no,0%x,(ivno.Top+ivno.Height)+2";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._no.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) ((mostCurrent._ivno.getTop()+mostCurrent._ivno.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (2),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 121;BA.debugLine="no.Visible = False";
mostCurrent._no.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 123;BA.debugLine="lf.Initialize(\"lf\")";
mostCurrent._lf.Initialize(mostCurrent.activityBA,"lf");
 //BA.debugLineNum = 124;BA.debugLine="lf.Text = \"Developed By Khun Htetz Naing\"";
mostCurrent._lf.setText((Object)("Developed By Khun Htetz Naing"));
 //BA.debugLineNum = 125;BA.debugLine="lf.Gravity = Gravity.CENTER";
mostCurrent._lf.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 126;BA.debugLine="lf.TextColor = Colors.Blue";
mostCurrent._lf.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Blue);
 //BA.debugLineNum = 127;BA.debugLine="Activity.AddView(lf,0%x,100%y - 130dip,100%x,10%";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lf.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (130))),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 128;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 200;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 202;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 196;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 198;BA.debugLine="End Sub";
return "";
}
public static String  _b_click() throws Exception{
 //BA.debugLineNum = 136;BA.debugLine="Sub b_Click";
 //BA.debugLineNum = 137;BA.debugLine="If p.SdkVersion > 19 Then";
if (mostCurrent._p.getSdkVersion()>19) { 
 //BA.debugLineNum = 138;BA.debugLine="t1.Enabled = True";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 140;BA.debugLine="ml.GetRoot";
mostCurrent._ml.GetRoot();
 //BA.debugLineNum = 141;BA.debugLine="If ml.HaveRoot Then";
if (mostCurrent._ml.HaveRoot) { 
 //BA.debugLineNum = 142;BA.debugLine="ivno.Visible = False";
mostCurrent._ivno.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 143;BA.debugLine="b.Visible = False";
mostCurrent._b.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 144;BA.debugLine="no.Visible = False";
mostCurrent._no.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 145;BA.debugLine="ivyes.Visible = True";
mostCurrent._ivyes.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 146;BA.debugLine="yes.Visible = True";
mostCurrent._yes.setVisible(anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 150;BA.debugLine="ivno.Visible = True";
mostCurrent._ivno.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 151;BA.debugLine="b.Visible = False";
mostCurrent._b.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 152;BA.debugLine="no.Visible = True";
mostCurrent._no.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 153;BA.debugLine="ivyes.Visible = False";
mostCurrent._ivyes.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 154;BA.debugLine="yes.Visible = False";
mostCurrent._yes.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 157;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 22;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 23;BA.debugLine="Dim About As Label";
mostCurrent._about = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 24;BA.debugLine="Dim ml As MLfiles";
mostCurrent._ml = new MLfiles.Fileslib.MLfiles();
 //BA.debugLineNum = 25;BA.debugLine="Dim b As Button";
mostCurrent._b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim p As Phone";
mostCurrent._p = new anywheresoftware.b4a.phone.Phone();
 //BA.debugLineNum = 27;BA.debugLine="Dim OS As String";
mostCurrent._os = "";
 //BA.debugLineNum = 28;BA.debugLine="Dim ivno,ivyes As ImageView";
mostCurrent._ivno = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._ivyes = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Dim imv As ImageView";
mostCurrent._imv = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Dim yes,no As Label";
mostCurrent._yes = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._no = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Dim banner As AdView";
mostCurrent._banner = new anywheresoftware.b4a.admobwrapper.AdViewWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Dim interstitial As mwAdmobInterstitial";
mostCurrent._interstitial = new mobi.mindware.admob.interstitial.AdmobInterstitialsAds();
 //BA.debugLineNum = 33;BA.debugLine="Dim lf As Label";
mostCurrent._lf = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 34;BA.debugLine="End Sub";
return "";
}
public static String  _imv_click() throws Exception{
 //BA.debugLineNum = 130;BA.debugLine="Sub imv_Click";
 //BA.debugLineNum = 131;BA.debugLine="If p.SdkVersion > 19 Then";
if (mostCurrent._p.getSdkVersion()>19) { 
 //BA.debugLineNum = 132;BA.debugLine="t1.Enabled = True";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 134;BA.debugLine="End Sub";
return "";
}
public static String  _no_click() throws Exception{
anywheresoftware.b4a.phone.Phone.PhoneIntents _i = null;
 //BA.debugLineNum = 159;BA.debugLine="Sub no_Click";
 //BA.debugLineNum = 160;BA.debugLine="If p.SdkVersion > 19 Then";
if (mostCurrent._p.getSdkVersion()>19) { 
 //BA.debugLineNum = 161;BA.debugLine="t1.Enabled = True";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 163;BA.debugLine="Dim i As PhoneIntents";
_i = new anywheresoftware.b4a.phone.Phone.PhoneIntents();
 //BA.debugLineNum = 164;BA.debugLine="StartActivity(i.OpenBrowser(\"https://play.google.";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.OpenBrowser("https://play.google.com/store/apps/details?id=com.htetznaing.roottools")));
 //BA.debugLineNum = 165;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 16;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="Dim t1,t2 As Timer";
_t1 = new anywheresoftware.b4a.objects.Timer();
_t2 = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 20;BA.debugLine="End Sub";
return "";
}
public static String  _t1_tick() throws Exception{
 //BA.debugLineNum = 175;BA.debugLine="Sub t1_Tick";
 //BA.debugLineNum = 176;BA.debugLine="If interstitial.Status = interstitial.Status_AdRea";
if (mostCurrent._interstitial.Status==mostCurrent._interstitial.Status_AdReadyToShow) { 
 //BA.debugLineNum = 177;BA.debugLine="interstitial.Show";
mostCurrent._interstitial.Show(mostCurrent.activityBA);
 };
 //BA.debugLineNum = 180;BA.debugLine="If interstitial.Status = interstitial.Status_Dismi";
if (mostCurrent._interstitial.Status==mostCurrent._interstitial.Status_Dismissed) { 
 //BA.debugLineNum = 181;BA.debugLine="interstitial.LoadAd";
mostCurrent._interstitial.LoadAd(mostCurrent.activityBA);
 };
 //BA.debugLineNum = 183;BA.debugLine="t1.Enabled = False";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 184;BA.debugLine="End Sub";
return "";
}
public static String  _t2_tick() throws Exception{
 //BA.debugLineNum = 186;BA.debugLine="Sub t2_Tick";
 //BA.debugLineNum = 187;BA.debugLine="If interstitial.Status = interstitial.Status_AdRe";
if (mostCurrent._interstitial.Status==mostCurrent._interstitial.Status_AdReadyToShow) { 
 //BA.debugLineNum = 188;BA.debugLine="interstitial.Show";
mostCurrent._interstitial.Show(mostCurrent.activityBA);
 };
 //BA.debugLineNum = 191;BA.debugLine="If interstitial.Status = interstitial.Status_Dismi";
if (mostCurrent._interstitial.Status==mostCurrent._interstitial.Status_Dismissed) { 
 //BA.debugLineNum = 192;BA.debugLine="interstitial.LoadAd";
mostCurrent._interstitial.LoadAd(mostCurrent.activityBA);
 };
 //BA.debugLineNum = 194;BA.debugLine="End Sub";
return "";
}
public static String  _yes_click() throws Exception{
anywheresoftware.b4a.phone.Phone.PhoneIntents _i = null;
 //BA.debugLineNum = 167;BA.debugLine="Sub yes_Click";
 //BA.debugLineNum = 168;BA.debugLine="If p.SdkVersion > 19 Then";
if (mostCurrent._p.getSdkVersion()>19) { 
 //BA.debugLineNum = 169;BA.debugLine="t1.Enabled = True";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 171;BA.debugLine="Dim i As PhoneIntents";
_i = new anywheresoftware.b4a.phone.Phone.PhoneIntents();
 //BA.debugLineNum = 172;BA.debugLine="StartActivity(i.OpenBrowser(\"https://play.google.";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.OpenBrowser("https://play.google.com/store/apps/details?id=com.htetznaing.rootchecker")));
 //BA.debugLineNum = 173;BA.debugLine="End Sub";
return "";
}
}
