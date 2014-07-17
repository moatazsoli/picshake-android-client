package com.moataz.picshake;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TutorialFragment extends Fragment{
	
	int countDemo=1;
	int visible = View.GONE;
	
	
//	public static final TutorialFragment newInstance()
//	{
//		TutorialFragment f = new TutorialFragment();
//		visible = View.INVISIBLE;
//	    return f;
//	}
	
	
	
	public TutorialFragment(){
		super();
		visible = View.GONE;
	}
	
	public TutorialFragment(int visibility){
		super();
		visible = visibility;
	}
	
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
		  	final View view = inflater.inflate(R.layout.overlay_activity, container, false);
			final Dialog dialog = new Dialog(getActivity(),
			android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			dialog.setContentView(R.layout.overlay_activity);
		//	 view.setVisibility(visible);
			final LinearLayout layout = (LinearLayout) dialog
			.findViewById(R.id.llOverlay_activity);
//			ImageView img = (ImageView) findViewById(R.drawable.startup1);
//			layout.addView(img);
			ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
			im.setImageResource(R.drawable.startup1);
			layout.removeAllViews();
			layout.addView(im);
			layout.setBackgroundColor(Color.TRANSPARENT);
			view.setBackgroundResource(R.drawable.startup1);
			layout.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View arg0) {
					ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
					layout.removeAllViews();
					countDemo++;
					switch (countDemo){
					
						case 2:
							im.setImageResource(R.drawable.startup2);					
							layout.addView(im);
							break;
						case 3:
							im.setImageResource(R.drawable.startup3);					
							layout.addView(im);
							break;
						case 4:
							im.setImageResource(R.drawable.startup4);					
							layout.addView(im);
							break;
						case 5:
							im.setImageResource(R.drawable.startup5);					
							layout.addView(im);
							break;
						case 6:
							im.setImageResource(R.drawable.startup6);					
							layout.addView(im);
							break;
						case 7:
							im.setImageResource(R.drawable.startup7);					
							layout.addView(im);
							break;
						case 8:
							im.setImageResource(R.drawable.startup8);					
							layout.addView(im);
							break;
						case 9:
							im.setImageResource(R.drawable.startup9);					
							layout.addView(im);
							break;
						case 10:							
							dialog.dismiss();	
							view.setVisibility(View.GONE);
							return;
						default:
							dialog.dismiss();
						
					}
					
				}
		
				});
	
			dialog.show();
		  
		// Inflate the layout for this fragment
	        return view;
	  }
//		super.onCreate(savedInstanceState);
//		final Dialog dialog = new Dialog(this,
//		android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//		dialog.setContentView(R.layout.overlay_activity);
//
//		final LinearLayout layout = (LinearLayout) dialog
//		.findViewById(R.id.llOverlay_activity);
////		ImageView img = (ImageView) findViewById(R.drawable.startup1);
////		layout.addView(img);
//		ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
//		im.setImageResource(R.drawable.startup1);
//		layout.removeAllViews();
//		layout.addView(im);
//		layout.setBackgroundColor(Color.TRANSPARENT);
//		layout.setOnClickListener(new OnClickListener() {
//
//		@Override
//		public void onClick(View arg0) {
//			ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
//			layout.removeAllViews();
//			countDemo++;
//			switch (countDemo){
//			
//				case 2:
//					im.setImageResource(R.drawable.startup2);					
//					layout.addView(im);
//					break;
//				case 3:
//					im.setImageResource(R.drawable.startup3);					
//					layout.addView(im);
//					break;
//				case 4:
//					im.setImageResource(R.drawable.startup4);					
//					layout.addView(im);
//					break;
//				case 5:
//					im.setImageResource(R.drawable.startup5);					
//					layout.addView(im);
//					break;
//				case 6:
//					im.setImageResource(R.drawable.startup6);					
//					layout.addView(im);
//					break;
//				case 7:
//					im.setImageResource(R.drawable.startup7);					
//					layout.addView(im);
//					break;
//				case 8:
//					dialog.dismiss();	
//					return;
//				default:
//					dialog.dismiss();
//				
//			}
//			
//		}
//
//		});
//
//		dialog.show();
//		}

}
