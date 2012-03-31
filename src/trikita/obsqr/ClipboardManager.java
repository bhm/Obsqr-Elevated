package trikita.obsqr;

import android.os.Build;
import android.content.Context;

/**
 * This class provides a basic wrapper around the built-in ClipboardManager
 * class that manages copying data to and from the system clipboard.  This
 * provides a wrapper around the API-specific versions of the class to return
 * the proper object for the platform we're currently running on.
 */
public abstract class ClipboardManager {
	protected static Context mContext;

	public abstract void setText(CharSequence text);

	public static ClipboardManager newInstance(Context ctx) {
		mContext = ctx;

		final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < Build.VERSION_CODES.HONEYCOMB)
			return new OldClipboardManager();
		else return new HoneycombClipboardManager();
	}
	
	/**
	 * The old ClipboardManager, which is a under android.text.  This is
	 * the version to use for all Android versions less than 3.0. 
	 */
	private static class OldClipboardManager extends ClipboardManager {
		private static android.text.ClipboardManager clippy = null;
		
		public OldClipboardManager() {
			clippy = (android.text.ClipboardManager)mContext.getSystemService(
					android.content.Context.CLIPBOARD_SERVICE);
		}
		
		@Override
		public void setText(CharSequence text) {
			clippy.setText(text);
		}
	}
	
	private static class HoneycombClipboardManager extends ClipboardManager {
		private static android.content.ClipboardManager clippy = null;
		private static android.content.ClipData clipData = null;
		
		public HoneycombClipboardManager() {
			clippy = (android.content.ClipboardManager)mContext.getSystemService(
					android.content.Context.CLIPBOARD_SERVICE);
		}
		
		@Override
		public void setText(CharSequence text) {
			clipData = android.content.ClipData.newPlainText(
					android.content.ClipDescription.MIMETYPE_TEXT_PLAIN, text);
			clippy.setPrimaryClip(clipData);
		}
	}

}
