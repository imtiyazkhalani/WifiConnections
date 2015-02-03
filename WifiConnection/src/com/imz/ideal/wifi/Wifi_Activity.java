package com.imz.ideal.wifi;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Wifi_Activity extends ActionBarActivity implements
		OnItemClickListener {

	private static final String TAG = "Wifi_Activity";

	private Switch wifi_swtich;
	private WifiManager wifiManager;
	private ListView wifi_list;

	private List<MYScanResult> myScanResults;
	private ArrayAdapter<MYScanResult> wifiAdapter;

	private TextView wifi_info;

	private String wifi_pass = "";
	private int is_wifi_protected = 0;
	private String wifi_ssid = "";

	private WifiReceiver mWifiReceiver;

	private boolean isItemClicked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);

		IntentFilter wifiFilters = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(mWifiReceiver, wifiFilters);

		initViews();
	}

	protected void onPause() {
		unregisterReceiver(mWifiReceiver);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(mWifiReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	private void initViews() {
		initData();

		wifi_list = (ListView) findViewById(R.id.wifi_listView);
		wifi_list.setOnItemClickListener(this);
		wifi_list.setAdapter(wifiAdapter);

		wifi_info = (TextView) findViewById(R.id.textView1);

		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		wifi_swtich = (Switch) findViewById(R.id.wifi_switch);
		// update switch status
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			wifi_swtich.setChecked(true);
			onWifiOn();
		} else {
			wifi_swtich.setChecked(false);
			Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG)
					.show();
		}

		// set adapters and envets to wifi list
		wifi_swtich.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(getApplicationContext(), "Wifi turned On",
							Toast.LENGTH_LONG).show();

					onWifiOn();
					// checkForWifi();

				} else {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(getApplicationContext(), "Wifi turned Off",
							Toast.LENGTH_LONG).show();
					wifi_list.setVisibility(View.GONE);
					wifi_info.setVisibility(View.GONE);
					findViewById(R.id.progressBar1).setVisibility(View.GONE);
				}
			}
		});

	}

	private void initData() {

		myScanResults = new ArrayList<MYScanResult>();

		wifiAdapter = new ArrayAdapter<MYScanResult>(this,
				android.R.layout.simple_list_item_1, myScanResults);

		mWifiReceiver = new WifiReceiver();
	}

	/**
	 * call this method when wifi is on to manage
	 */
	private void onWifiOn() {
		Log.e(TAG, "wifiManager START SCANNING");
		findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
		myScanResults.clear();
		wifiAdapter.notifyDataSetChanged();
		wifiManager.startScan();
		wifi_list.setVisibility(View.VISIBLE);
	}

	public int getWifyKeyMgmt(ScanResult result) {
		String Capabilities = result.capabilities;
		if (Capabilities.contains("WPA")) {
			return WifiConfiguration.KeyMgmt.WPA_PSK;
		} else if (Capabilities.contains("WEP")) {
			return WifiConfiguration.GroupCipher.WEP40;
		} else {
			return WifiConfiguration.KeyMgmt.NONE;
		}
	}

	private void askForDialog() {

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.row2);
		dialog.setTitle("Enter Password of " + wifi_ssid);
		dialog.findViewById(R.id.btn_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						EditText etPass = (EditText) dialog
								.findViewById(R.id.edit_pass);
						wifi_pass = etPass.getText().toString().trim();
						dialog.cancel();

						ConnectToWifi(wifi_pass);
					}
				});

		dialog.findViewById(R.id.btn_cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.cancel();
					}
				});
		dialog.show();
	}

	public void ConnectToWifi(String pass_code) {

		WifiConfiguration conf = new WifiConfiguration();
		// Please note the quotes. String should contain ssid in quotes
		conf.SSID = "\"" + wifi_ssid + "\"";

		// conf.wepKeys[0] = "\"" + wifi_pass + "\"";
		// conf.wepTxKeyIndex = 0;
		// conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		// conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

		if (is_wifi_protected == WifiConfiguration.KeyMgmt.WPA_PSK) {
			// pass
			conf.preSharedKey = "\"" + wifi_pass + "\"";
		} else {
			// no pass
			conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		}

		wifiManager.addNetwork(conf);

		//
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + wifi_ssid + "\"")) {
				connectedCongif = i;
				wifiManager.disconnect();
				wifiManager.enableNetwork(i.networkId, true);
				wifiManager.reconnect();
				break;
			}
		}
		mWifiConnectionHandler.sendEmptyMessageDelayed(0, 3 * 1000);

		// final Handler mHandler = new Handler();
		// mHandler.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// }
		// }, 1 * 1000);

		Toast.makeText(getApplicationContext(), "Connecting to " + wifi_ssid,
				Toast.LENGTH_LONG).show();
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			if (!isItemClicked) {
				if (wifi_list.getVisibility() == View.GONE) {
					wifi_list.setVisibility(View.VISIBLE);
					findViewById(R.id.progressBar1).setVisibility(View.GONE);
				}
				List<ScanResult> tempwifiList = wifiManager.getScanResults();
				for (int i = 0; i < tempwifiList.size(); i++) {
					if ((tempwifiList.get(i)).SSID == null
							|| (tempwifiList.get(i)).SSID.equals("")) {
						// Not Valid Entry
					} else {
						addResultIfNotInList(tempwifiList.get(i));
					}
				}
				wifiAdapter.notifyDataSetChanged();
			}
		}
	}

	private WifiConfiguration connectedCongif;
	private Handler mWifiConnectionHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			Log.e(TAG, "handleMessage");

			if (wifiManager.getConnectionInfo() == null) {

				List<WifiConfiguration> list = wifiManager
						.getConfiguredNetworks();
				for (WifiConfiguration i : list) {
					if (i.SSID != null
							&& i.SSID.equals("\"" + wifi_ssid + "\"")) {
						connectedCongif = i;
						wifiManager.disconnect();
						wifiManager.enableNetwork(i.networkId, true);
						wifiManager.reconnect();
						break;
					}
				}
				// mWifiConnectionHandler.sendEmptyMessageDelayed(0, 5 * 1000);
			} else {

				WifiInfo info = wifiManager.getConnectionInfo();

				// if (info.getSupplicantState() != SupplicantState.SCANNING) {
				// wifiManager.disconnect();
				// wifiManager.enableNetwork(connectedCongif.networkId, true);
				// wifiManager.reconnect();
				// } else
				if (info.getSupplicantState() == SupplicantState.SCANNING) {

					List<WifiConfiguration> list = wifiManager
							.getConfiguredNetworks();
					for (WifiConfiguration i : list) {
						if (i.SSID != null
								&& i.SSID.equals("\"" + wifi_ssid + "\"")) {
							connectedCongif = i;
							wifiManager.disconnect();
							wifiManager.enableNetwork(i.networkId, true);
							wifiManager.reconnect();
							break;
						}
					}

					wifi_info.setText(wifi_info.getText() + "\nSCANNING");

					mWifiConnectionHandler.sendEmptyMessageDelayed(0, 3 * 1000);
					Log.e(TAG, "SupplicantState.SCANNING");
				} else if (info.getSupplicantState() == SupplicantState.AUTHENTICATING) {
					wifi_info.setText(wifi_info.getText() + "\nAutheticating");
					mWifiConnectionHandler.sendEmptyMessageDelayed(0, 2 * 1000);
					// Toast.makeText(Wifi_Activity.this, "Autheticating",
					// Toast.LENGTH_SHORT).show();
				} else if (info.getSupplicantState() == SupplicantState.UNINITIALIZED) {
					wifi_info.setText(wifi_info.getText()
							+ "\nFailed Authentication");
					// Toast.makeText(Wifi_Activity.this,
					// "Failed Authentication",
					// Toast.LENGTH_SHORT).show();
				} else if (info.getSupplicantState() == SupplicantState.COMPLETED) {
					wifi_info.setText(wifi_info.getText() + "\nConnected");
				} else if (info.getSupplicantState() == SupplicantState.ASSOCIATED) {
					wifi_info.setText(wifi_info.getText()
							+ "\nAssociation completed.");
					mWifiConnectionHandler.sendEmptyMessageDelayed(0, 2 * 1000);
				} else if (info.getSupplicantState() == SupplicantState.FOUR_WAY_HANDSHAKE) {
					wifi_info.setText(wifi_info.getText()
							+ "\nWPA 4-Way Key Handshake in progress.");
					mWifiConnectionHandler.sendEmptyMessageDelayed(0, 2 * 1000);
				}
				Log.e(TAG, info.toString() + "");
			}
			// else {
			// if (connectedCongif != null) {
			//
			// } else {
			// wifiManager.disconnect();
			// wifiManager = (WifiManager)
			// getSystemService(Context.WIFI_SERVICE);
			// wifiManager.enableNetwork(connectedCongif.networkId, true);
			// wifiManager.reconnect();

			// }

			// }

		}
	};

	private void addResultIfNotInList(ScanResult result) {
		if (result != null && result.SSID != null && result.SSID.length() > 0) {
			if (myScanResults.size() != 0) {
				boolean isAlready = false;
				for (MYScanResult myResult : myScanResults) {
					Log.e(TAG, "COMPARE :" + result.SSID + " TO myResult::"
							+ myResult.result.SSID);
					if (result.SSID.equals(myResult.result.SSID)) {
						isAlready = true;
						break;
					}
				}

				if (!isAlready) {
					myScanResults.add(new MYScanResult(result, result.SSID));
				}
			} else {
				myScanResults.add(new MYScanResult(result, result.SSID));
			}
		}
	}

	private class MYScanResult {
		public final ScanResult result;
		public final String wifiName;

		public MYScanResult(ScanResult result, String wifiName) {
			this.result = result;
			this.wifiName = wifiName;
		}

		@Override
		public String toString() {
			return wifiName;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		isItemClicked = true;
		final ScanResult result = myScanResults.get(position).result;
		wifi_ssid = result.SSID;

		// Toast.makeText(getApplicationContext(),"",
		// Toast.LENGTH_LONG).show();

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			// Do whatever
			// Toast.makeText(getApplicationContext(),"Already connected to a network",
			// Toast.LENGTH_LONG).show();

			AlertDialog.Builder builder = new AlertDialog.Builder(
					Wifi_Activity.this);

			builder.setTitle("Wifi");
			builder.setMessage("You are already connected to a network. Do you want to connect to another wifi ?");
			builder.setCancelable(false);

			builder.setPositiveButton("YES",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							// Code that is executed when clicking YES

							dialog.dismiss();
							wifiManager.disconnect();
							int i = getWifyKeyMgmt(result);
							if (i == WifiConfiguration.KeyMgmt.WPA_PSK) {

								is_wifi_protected = WifiConfiguration.KeyMgmt.WPA_PSK;
								askForDialog();

							} else {

								is_wifi_protected = WifiConfiguration.KeyMgmt.NONE;
								ConnectToWifi(wifi_pass);
							}
						}

					});

			builder.setNegativeButton("NO",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							// Code that is executed when clicking NO

							dialog.dismiss();
						}

					});

			AlertDialog alert = builder.create();
			alert.show();
		} else {
			int i = getWifyKeyMgmt(result);
			if (i == WifiConfiguration.KeyMgmt.WPA_PSK) {

				is_wifi_protected = WifiConfiguration.KeyMgmt.WPA_PSK;
				askForDialog();

			} else {

				is_wifi_protected = WifiConfiguration.KeyMgmt.NONE;
				ConnectToWifi(wifi_pass);
			}
		}
	}

	// /**
	// * for un registering receivers
	// */
	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// unregisterReceiver(mWifiReceiver);
	// }

}
