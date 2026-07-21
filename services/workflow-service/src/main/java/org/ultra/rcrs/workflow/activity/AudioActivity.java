package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface AudioActivity {

    void checkAllAudiosUploaded(List<String> uids);
}
