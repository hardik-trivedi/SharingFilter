package com.example.sharingfilter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	EditText shareText;
	Button shareAll, shareFiltered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		shareText = (EditText) findViewById(R.id.shareText);
		shareAll = (Button) findViewById(R.id.shareAll);
		shareFiltered = (Button) findViewById(R.id.shareFiltered);

		shareAll.setOnClickListener(this);
		shareFiltered.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void shareAll() {

		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.intent_subject));
		sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText.getText()
				.toString());
		startActivity(Intent.createChooser(sharingIntent,
				getString(R.string.app_name)
						+ getString(R.string.share_message)));

	}

	private void onShareFiltered() {
		Intent emailIntent = new Intent();
		emailIntent.setAction(Intent.ACTION_SEND);
		// Native email client doesn't currently support HTML, but it doesn't
		// hurt to try in case they fix it
		emailIntent.putExtra(Intent.EXTRA_TEXT, shareText.getText().toString());
		emailIntent.putExtra(Intent.EXTRA_SUBJECT,
				getString(R.string.intent_subject));
		emailIntent.setType("message/rfc822");

		PackageManager pm = getPackageManager();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");

		Intent openInChooser = Intent.createChooser(emailIntent,
				getString(R.string.app_name)
						+ getString(R.string.share_message));

		List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
		List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
		for (int i = 0; i < resInfo.size(); i++) {
			// Extract the label, append it, and repackage it in a LabeledIntent
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			if (packageName.contains("android.email")) {
				emailIntent.setPackage(packageName);
			} else if (packageName.contains("twitter")
					|| packageName.contains("facebook")) {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(packageName,
						ri.activityInfo.name));
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				if (packageName.contains("twitter")) {
					intent.putExtra(Intent.EXTRA_TEXT, shareText.getText()
							.toString());
				} else if (packageName.contains("facebook")) {
					// Warning: Facebook IGNORES our text. They say
					// "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
					// One workaround is to use the Facebook SDK to post, but
					// that doesn't allow the user to choose how they want to
					// share. We can also make a custom landing page, and the
					// link
					// will show the <meta content ="..."> text from that page
					// with our link in Facebook.
					intent.putExtra(Intent.EXTRA_TEXT, shareText.getText()
							.toString());
				} else if (packageName.contains("android.gm")) {
					intent.putExtra(Intent.EXTRA_TEXT, shareText.getText()
							.toString());
					intent.putExtra(Intent.EXTRA_SUBJECT,
							getString(R.string.intent_subject));
					intent.setType("message/rfc822");
				}

				intentList.add(new LabeledIntent(intent, packageName, ri
						.loadLabel(pm), ri.icon));
			}
		}

		// convert intentList to array
		LabeledIntent[] extraIntents = intentList
				.toArray(new LabeledIntent[intentList.size()]);

		openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
		startActivity(openInChooser);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == shareAll) {
			shareAll();
		} else if (v == shareFiltered) {
			onShareFiltered();
		}
	}
}
