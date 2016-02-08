/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.view.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bbqapp.android.R;
import org.bbqapp.android.api.model.Id;
import org.bbqapp.android.api.model.Picture;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.model.Point;
import org.bbqapp.android.api.service.PlaceService;
import org.bbqapp.android.service.GeocodeService;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.view.BaseFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Fragment to create new places
 */
public class CreateFragment extends BaseFragment {
    private static final String TAG = CreateFragment.class.getName();

    private static final int TAKE_PICTURE_REQUEST_CODE = 1;

    private View view;

    @Bind(R.id.view_create_take_picture)
    Button takePictureButton;
    @Bind(R.id.view_create_create)
    Button createButton;
    @Bind(R.id.view_create_picture)
    ImageView imageView;
    @Bind(R.id.view_create_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.view_create_progress_text)
    TextView progressText;
    @Bind(R.id.view_create_location)
    EditText locationEditText;
    @Bind(R.id.view_create_address)
    TextView addressText;

    private File image = null;

    @Inject
    GeocodeService geocodeService;

    @Inject
    LocationService locationService;

    @Inject
    PlaceService placeService;
    @Inject
    @Named("main")
    Scheduler mainScheduler;
    @Inject
    @Named("io")
    Scheduler ioScheduler;

    private Subscription subscriber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.view_create, container, false);
            ButterKnife.bind(this, view);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.menu_create);

        Observable<Location> filteredLocation = locationService.getLocation().filter(new Func1<Location, Boolean>() {
            @Override
            public Boolean call(Location location) {
                return location.getAccuracy() <= 20 && (System.currentTimeMillis() - location.getTime()) <= 60_000;
            }
        });
        filteredLocation.take(1).observeOn(mainScheduler).subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                locationEditText.setText(String.format("%s, %s", location.getLatitude(), location.getLongitude()));
            }
        });
        subscriber = geocodeService
                .resolve(filteredLocation)
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .take(1)
                .observeOn(mainScheduler)
                .subscribe(new Action1<Address>() {
                    @Override
                    public void call(Address address) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i));
                            sb.append("\n");
                        }
                        sb.deleteCharAt(sb.length() - 1);

                        addressText.setText(sb.toString());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        addressText.setText(throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        view = null;

        subscriber.unsubscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE_REQUEST_CODE:
                try {
                    onTakePictureResponse(resultCode);
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showMessage(final String message) {
        final Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setProgress(final String msg, final Integer progress) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String dspMsg = msg;
                    if (progress != null) {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(progress);
                        dspMsg += " " + progress + "%";
                    } else {
                        progressBar.setIndeterminate(true);
                    }
                    progressText.setText(dspMsg);
                }
            });
        }
    }

    @OnClick(R.id.view_create_take_picture)
    protected void takePicture() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        try {
            image = File.createTempFile("takePicture_", ".jpg", dir);

            Log.i(TAG, "take picture to: " + Uri.fromFile(image));

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);

        } catch (IOException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void onTakePictureResponse(int resultCode) throws IOException {
        if (resultCode == Activity.RESULT_OK && image != null) {

            Uri imageUri = Uri.fromFile(image);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;// > 1
            Bitmap imageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            imageView.setImageBitmap(imageBitmap);
            Log.i(TAG, "onTakePictureResponse, image saved to: " + imageUri);
        }
    }

    @OnClick(R.id.view_create_create)
    protected void create() {
        Point location = new Point(locationEditText.getText().toString());

        final Place place = new Place();
        place.setLocation(location);

        setProgress("Preparing...", null);

        placeService.postPlace(place)
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(new Subscriber<Id>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showMessage("Could not create new place");
                        setProgress("Could not create new place", null);
                        Timber.e(e, "Could not create new place");
                    }

                    @Override
                    public void onNext(Id id) {
                        setProgress("Place Created: " + id.getId(), image == null ? 100 : null);
                        if (image == null) {
                            return;
                        }

                        uploadImage(id);
                    }
                });
    }

    private void uploadImage(final Id id) {
        Picture picture;
        try {
            picture = new Picture(image) {
                @Override
                public void onProgress(long contentLength, long transferred) {
                    int percent = (int) (transferred / (contentLength / 100));
                    setProgress("Upload image...", percent);
                }
            };
        } catch (FileNotFoundException e) {
            setProgress("Error occurred while image processing...", null);
            showMessage("Error occurred while image processing");
            Timber.e(e, "Error occurred while image processing");
            return;
        }
        placeService.postPicture(id, picture)
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(new Subscriber<Id>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showMessage("Could not upload place picture");
                        setProgress("Could not upload place picture", null);
                        Log.e(TAG, "Could not upload place picture", e);
                    }

                    @Override
                    public void onNext(Id id) {
                        setProgress("Image uploaded: " + id.getId(), 100);
                    }
                });
    }
}
