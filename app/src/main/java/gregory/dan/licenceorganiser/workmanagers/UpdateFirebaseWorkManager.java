package gregory.dan.licenceorganiser.workmanagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import gregory.dan.licenceorganiser.Unit.Ammunition;
import gregory.dan.licenceorganiser.Unit.Inspection;
import gregory.dan.licenceorganiser.Unit.Licence;
import gregory.dan.licenceorganiser.Unit.OutstandingPoints;
import gregory.dan.licenceorganiser.Unit.Unit;
import gregory.dan.licenceorganiser.firebase.FireBaseDatabaseUtilities;
import gregory.dan.licenceorganiser.notifications.NotificationUtility;

import static gregory.dan.licenceorganiser.Constants.OBJECT_DATA_KEY;
import static gregory.dan.licenceorganiser.Constants.OBJECT_TYPE;

public class UpdateFirebaseWorkManager extends Worker {

    public UpdateFirebaseWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        Gson gson = new Gson();
        String data = getInputData().getString(OBJECT_DATA_KEY);
        String type = getInputData().getString(OBJECT_TYPE);
        try {
            FireBaseDatabaseUtilities fireBaseDatabaseUtilities = new FireBaseDatabaseUtilities();
            if (type != null && !type.equals("")) {
                if (type.equals("unit")) {
                    Unit object = gson.fromJson(data, Unit.class);
                    fireBaseDatabaseUtilities.inseertOrUpdateFirebaseUnit(object);
                } else if (type.equals("outstanding_point")) {
                    OutstandingPoints object = gson.fromJson(data, OutstandingPoints.class);
                    fireBaseDatabaseUtilities.inseertOrUpdateFirebasePoint(object);
                } else if (type.equals("licence")) {
                    Licence object = gson.fromJson(data, Licence.class);
                    fireBaseDatabaseUtilities.inseertOrUpdateFirebaseLicence(object);
                } else if (type.equals("inspection")) {
                    Inspection object = gson.fromJson(data, Inspection.class);
                    fireBaseDatabaseUtilities.inseertOrUpdateFirebaseInspection(object);
                } else if (type.equals("ammunition")) {
                    Ammunition object = gson.fromJson(data, Ammunition.class);
                    fireBaseDatabaseUtilities.inseertOrUpdateFirebaseAmmunition(object);
                }
            } else {
                NotificationUtility.makeStatusNotification("firebase update failed", applicationContext);
                return Result.failure();
            }

            NotificationUtility.makeStatusNotification("firebase updated", applicationContext);

            return Result.success();
        } catch (Throwable e) {
            Log.e("WorkManager: ", e.toString());
            return Result.failure();
        }
    }
}
