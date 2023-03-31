package com.example.software2.ocrhy;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import androidx.annotation.Nullable;
public class GetAllData extends IntentService {
    private static final String IDENTIFIER = "GetAddressIntentService";
    //An identifier is a name that identifies either a unique object or a unique class of objects from another java activities
    private ResultReceiver addressResultReceiver;
    //create object ResultReciever to recive the address result
    private TextToSpeech texttospeech;

    public GetAllData() {
        super(IDENTIFIER);
        /*super keyword is used to access methods of the parent class
       while this is used to access methods of the current class.
       this is a reserved keyword in java i.e, we can't use it as
         an identifier. this is used to refer current-class's instance
        as well as static members.*/
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //onHandleIntent is a intent service for passing the data
        String msg;
        addressResultReceiver = Objects.requireNonNull(intent).getParcelableExtra("add_receiver");
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "No receiver, not processing the request further");
            return;
        }
        Location location = intent.getParcelableExtra("add_location");
        if (location == null) {
            msg = "No location, can't go further without location";
            texttospeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
            sendResultsToReceiver(0, msg);
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //Geocoding refers to transforming street address or any address
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }
        catch (Exception ioException) {
            Log.e("", "Error in getting address for the location");
        }
        if (addresses == null || addresses.size() == 0) {
            msg = "No address found for the location";
            sendResultsToReceiver(1, msg);
        }
        else {
            Address address = addresses.get(0);
            String addressDetails = address.getFeatureName() +"."+  "\n" +
                    "Locality is, " + address.getLocality() + "."+ "\n" + "City is ," + address.getSubAdminArea()+"." + "\n" +
                    "State is, " + address.getAdminArea()+"."+ "\n" + "Country is, " + address.getCountryName() +"."+ "\n";
            sendResultsToReceiver(2, addressDetails);
        }
    }
    private void sendResultsToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("address_result", message);
        addressResultReceiver.send(resultCode, bundle);
    }
}