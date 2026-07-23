package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface AudioActivity {

    @ActivityMethod
    void checkAllAudiosUploaded(List<String> uids);
}
