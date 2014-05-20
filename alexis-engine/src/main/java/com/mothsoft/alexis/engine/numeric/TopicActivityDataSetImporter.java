package com.mothsoft.alexis.engine.numeric;

import java.util.Date;

public interface TopicActivityDataSetImporter extends DataSetImporter {

    public void importTopicDataForUser(final Long userId, final Date startDate, final Date endDate);

}