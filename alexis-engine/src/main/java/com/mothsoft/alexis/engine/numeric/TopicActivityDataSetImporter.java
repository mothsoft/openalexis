package com.mothsoft.alexis.engine.numeric;

import java.math.BigInteger;
import java.util.Date;

public interface TopicActivityDataSetImporter extends DataSetImporter {

    public BigInteger importTopicDataForTopic(Long userId, Long id, Date startDate, Date endDate);

}