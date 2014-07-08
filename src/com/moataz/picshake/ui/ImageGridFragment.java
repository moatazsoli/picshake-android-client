/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moataz.picshake.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.example.android.displayingbitmaps.provider.Images;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.example.android.displayingbitmaps.util.Utils;
import com.moataz.picshake.BuildConfig;
import com.moataz.picshake.R;
import com.moataz.picshake.R.dimen;
import com.moataz.picshake.R.drawable;
import com.moataz.picshake.R.id;
import com.moataz.picshake.R.layout;
import com.moataz.picshake.R.menu;
import com.moataz.picshake.R.string;
import com.moataz.picshake.ReceiverActivity;
import com.moataz.picshake.SingleMediaScanner;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    HashMap<Integer, String> selectedPositions = new HashMap<Integer, String>();
    private Drawable checkmarkDrawable;
    private Bitmap checkmarkBitmap;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private GridView mGridView;
    private Cursor cursor;
    private int columnIndex;
    private ArrayList <String> picsUrls;
    private ArrayList <String> thumbsUrls;
    
    private int numCols=0;
    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle bundle = this.getArguments();
    	if(bundle != null){
    		picsUrls = bundle.getStringArrayList("pics");
    		thumbsUrls = bundle.getStringArrayList("thumbs");
    	}
    	
        setHasOptionsMenu(true);
        checkmarkBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.greencheckmark1)).getBitmap();
        checkmarkDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(checkmarkBitmap, 150, 150, false));
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        mAdapter = new ImageAdapter(getActivity());

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setMultiChoiceModeListener(new MultiChoiceModeListener());
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                numCols = mAdapter.getNumColumns();
                                mAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                                if (Utils.hasJellyBean()) {
                                    mGridView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                    mGridView.getViewTreeObserver()
                                            .removeGlobalOnLayoutListener(this);
                                }
                            }
                        }
                    }
                });

        return v;
    }
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public class MultiChoiceModeListener implements
			GridView.MultiChoiceModeListener {
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Items");
			mode.setSubtitle("One item selected");
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		@SuppressWarnings("unchecked")
		public void onDestroyActionMode(ActionMode mode) {
			if (selectedPositions.size() > 0) {
				// TODO add warning regarding sizes , maybe > 5 Mbs
				new DownloadImages().execute(selectedPositions.keySet());
			}
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			int selectCount = mGridView.getCheckedItemCount();
			switch (selectCount) {
			case 1:
				selectedPositions.clear();
				mode.setSubtitle("One item selected");
				break;
			default:
				mode.setSubtitle("" + selectCount + " items selected");
				break;
			}
			if (checked) {
				selectedPositions.put(position - numCols, "");
			} else {
				selectedPositions.remove(position - numCols);
			}

		}

	}

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
        i.putExtra("picsUrls",picsUrls);
        if (Utils.hasJellyBean()) {
            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
            // show plus the thumbnail image in GridView is cropped. so using
            // makeScaleUpAnimation() instead.
            ActivityOptions options =
                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public class CheckableLayout extends FrameLayout implements Checkable {
		private boolean mChecked;

		public CheckableLayout(Context imageAdapter) {
			super(imageAdapter);
		}

		@SuppressLint("NewApi")
		public void setChecked(boolean checked) {
			mChecked = checked;
//			setBackground(checked ? getResources().getDrawable(R.color.blue)
//					: null);
			setForeground(checked ?
					 checkmarkDrawable
					 : null);
		}

		public boolean isChecked() {
			return mChecked;
		}

		public void toggle() {
			setChecked(!mChecked);
		}

	}

    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private int mActionBarHeight = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageAdapter(Context context) {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, context.getResources().getDisplayMetrics());
            }
        }

        @Override
        public int getCount() {
            // If columns have yet to be determined, return no items
            if (getNumColumns() == 0) {
                return 0;
            }

            // Size + number of columns for top empty row
            return thumbsUrls.size() + mNumColumns;
        }

        @Override
        public Object getItem(int position) {
            return position < mNumColumns ?
                    null : thumbsUrls.get(position - mNumColumns);
        }

        @Override
        public long getItemId(int position) {
            return position < mNumColumns ? 0 : position - mNumColumns;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position < mNumColumns) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            //BEGIN_INCLUDE(load_gridview_item)
            // First check if this is the top row
            if (position < mNumColumns) {
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                
                TextView lTextView = new TextView(mContext);
                lTextView.setGravity(Gravity.CENTER);
                lTextView.setTextAppearance(mContext, android.R.attr.textAppearanceLarge);
                lTextView.setLayoutParams(new AbsListView.LayoutParams(
                        LayoutParams.MATCH_PARENT, mActionBarHeight));
                
                switch(position) {
                case 0:
                	lTextView.setText("Press  and");
                	break;
                case 1:
                	lTextView.setText("Hold  to");
                	break;
                case 2:
                	lTextView.setText("Select  pics");
                	break;
                }
                return lTextView;
                
            }

            // Now handle the main ImageView thumbnails
            CheckableLayout l;
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
                imageView = new RecyclingImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
                l = new CheckableLayout(mContext);
				l.setLayoutParams(mImageViewLayoutParams);
				l.addView(imageView);
            } else { // Otherwise re-use the converted view
                //imageView = (ImageView) convertView;
            	l = (CheckableLayout) convertView;
            	imageView = (ImageView) l.getChildAt(0);
            }

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
        	mImageFetcher.loadImage(thumbsUrls.get(position - mNumColumns), imageView);
            return l;
            //END_INCLUDE(load_gridview_item)
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
    }

	private class DownloadImages extends AsyncTask<Set<Integer>, String, Integer> {
			
			int count=1;
			private ProgressDialog mProgressDialog;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog.setMessage("Downloading Full Size Images. Please wait...");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setCanceledOnTouchOutside(false);
				//later on allow user to cancel and set the button callback to cancel and exit the download process
				//mProgressDialog.setCancelable(true);
				mProgressDialog.show();
			}
	
			@Override
			protected Integer doInBackground(Set<Integer>... list) {
				
				Set<Integer> items = list[0];
				final int size= items.size();
				String imageURL = "";
				Bitmap bitmap;
				HashMap<String, Object> tempMap;
				if (items.size() == 0) {
					return null;
				}
				String filepath = "";
				for (Iterator iterator = items.iterator(); iterator.hasNext();) {
					
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							mProgressDialog.setMessage("Downloading Full Size Images. Please wait... ("+count+"/"+size+")");
							count++;
						}
					});
					
					//tempMap = thumbnailsMap.get((Integer) iterator.next());
					
					imageURL = (String) picsUrls.get((Integer) iterator.next());
					// TODO check if imageURL is not NULL and do this for all iterators in project
					try {
						URL url = new URL(imageURL);
						HttpURLConnection urlConnection = (HttpURLConnection) url
								.openConnection();
						urlConnection.setRequestMethod("GET");
	//					urlConnection.setDoOutput(true);
						urlConnection.connect();
	
						String root = Environment.getExternalStorageDirectory()
								.toString();
	//					System.out.println("ROOOT" + root);
						File myDir = new File(root + "/Download/");
						myDir.mkdirs();
						Random generator = new Random();
						int n = 10000;
						n = generator.nextInt(n);
						String fname = "MYImage-" + n + ".jpg";
						File file = new File(myDir, fname);
						if (file.exists())
							file.delete();
	
						FileOutputStream fileOutput = new FileOutputStream(file);
						InputStream inputStream = urlConnection.getInputStream();
						long totalSize = urlConnection.getContentLength();
//						Integer totalSize = (Integer) tempMap.get("size");
						int downloadedSize = 0;
						byte[] buffer = new byte[1024];
						int bufferLength = 0;
						while ((bufferLength = inputStream.read(buffer)) > 0) {
							fileOutput.write(buffer, 0, bufferLength);
							downloadedSize += bufferLength;
							Log.i("Progress:", "downloadedSize:" + downloadedSize
									+ "totalSize:" + totalSize);
							//sometimes size returned from server is smaller than actual size
							if(downloadedSize <= totalSize)
							{
								publishProgress(""+(int)((downloadedSize*100)/totalSize));
							}else{
								publishProgress(""+100);
							}
						}
						fileOutput.close();
						new SingleMediaScanner(getActivity(), file);
						if (downloadedSize == totalSize)
							filepath = file.getPath();
					} catch (MalformedURLException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						filepath = null;
						e.printStackTrace();
						return null;
					}
					Log.i("filepath:", " " + filepath);
					
//					showNotification(count-1,size);
				}
	
				// fix this return
				return 1;
			}
	
			/**
	         * Updating progress bar
	         * */
	        protected void onProgressUpdate(String... progress) {
	            // setting progress percentage
	        	mProgressDialog.setProgress(Integer.parseInt(progress[0]));
	       }
	        
			@Override
			protected void onPostExecute(Integer result) {
				// Set the bitmap into ImageView
	//			image.setImageBitmap(result);
				mProgressDialog.dismiss();
				
				if(result == null)
				{
							Toast.makeText(getActivity(), "ERROR!!", 
									Toast.LENGTH_SHORT).show();
				}else{
					
					Toast.makeText(getActivity(), "Download Completed. Photos Saved To Gallery", 
							Toast.LENGTH_SHORT).show();
				}
				getActivity().finish();
				
				
			}
		}
}
