package com.fitbit.api;

import com.fitbit.api.client.http.OAuth;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.model.APICollectionType;
import com.fitbit.api.model.APIFormat;
import com.fitbit.api.model.APIVersion;
import com.fitbit.api.model.ApiCollectionProperty;
import com.fitbit.api.model.FitbitResourceOwner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;

public class APIUtil {

	private static final Log log = LogFactory.getLog(APIUtil.class);
	
	public static final String SIGNATURE_HEADER_NAME = "X-Fitbit-Signature";
	public static final String UNSPECIFIED_SUBSCRIPTION_ID = "";

	protected static final int TYPICAL_URL_LENGTH = 70;
	protected static final String[] DATE_FORMATS = new String[] { "yyyy-MM-dd", "yyyy-MM" };
	protected static final int STREAM_BUFFER_SIZE = 1024;


    public static String constructFullUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, LocalDate date, APIFormat format) {
    	return nullSafeConstructUrl(baseUrl, version, owner, collectionType, date, null, format);
    }

    public static String constructFullUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, ApiCollectionProperty collectionProperty, APIFormat format) {
        return nullSafeConstructUrl(baseUrl, version, owner, collectionType, collectionProperty, format);
    }

    public static String constructFullSubscriptionUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, APIFormat format) {
    	return constructFullSubscriptionUrl(baseUrl, version, owner, collectionType, UNSPECIFIED_SUBSCRIPTION_ID, format);
    }

    public static String constructFullSubscriptionUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, String subscriptionId, APIFormat format) {
    	return nullSafeConstructUrl(baseUrl, version, owner, collectionType, null, subscriptionId, format);
    }

    public static String constructRelativeUrl(FitbitResourceOwner owner, APICollectionType collectionType, LocalDate date) {
    	return nullSafeConstructRelativeUrl(owner, collectionType, date, null);
    }
    
    public static String contextualizeUrl(String baseUrl, APIVersion version, String relativeUrl, APIFormat format) {
    	return nullSafeContextualizeUrl(baseUrl, version, relativeUrl, format);
    }

    /**
     * Parses the given date string into a valid {@link LocalDate} object, 
     * suitable for passing into many API methods. 
     * 
     * @param date
     * @return
     * @throws IllegalArgumentException if the given date can not be parsed
     */
    public static LocalDate parseDate(String date) {
    	if (null==date || date.length() < 1) {
    		throw new IllegalArgumentException("Invalid empty input.");
    	}
    	
    	LocalDate result = null;
    	
    	for (String format : DATE_FORMATS) {
    		if (date.length()==format.length()) {
    			try {
	    			long time = DateTimeFormat.forPattern(format).parseMillis(date);
	    			result = new LocalDate(time);
	    			break;
    			} catch (Exception e) {
    				if (log.isDebugEnabled()) {
    					log.debug("Pattern '" + format + "' does not match date input '" + date + "': " + e);
    				}
    			}
    		}
    	}
    	
    	if (null==result) {
    		throw new IllegalArgumentException("Invalid input date: '" + date + "'");
    	}
    	
    	return result;
    }
    

    // TEST API inputStreamToString
    public static String inputStreamToString(InputStream is) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	
    	Reader reader = new InputStreamReader(is);
    	
    	char[] buffer = new char[STREAM_BUFFER_SIZE];
    	int count;
    	while ((count = reader.read(buffer)) > 0) {
    		sb.append(buffer, 0, count);
    	}
    	
    	return sb.toString();
    }


    public static String generateSignature(String data, String secret) {
    	OAuth oauth = new OAuth(null, secret);
    	return oauth.generateSignature(data);
    }

    
    /* ********************************************************************* */

    protected static String nullSafeConstructUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, LocalDate date, String subscriptionId, APIFormat format) {
    	return 
    		nullSafeContextualizeUrl(
    			baseUrl,
    			version,
    			nullSafeConstructRelativeUrl(
    				owner,
    				collectionType,
    				date,
    				subscriptionId
    			),
    			format
    		);
    }

    protected static String nullSafeConstructUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, APICollectionType collectionType, ApiCollectionProperty collectionProperty, APIFormat format) {
    	return
    		nullSafeContextualizeUrl(
    			baseUrl,
    			version,
    			nullSafeConstructRelativeUrl(
    				owner,
    				collectionType,
    				collectionProperty
    			),
    			format
    		);
    }

    private static String nullSafeConstructRelativeUrl(FitbitResourceOwner owner, APICollectionType collectionType, ApiCollectionProperty collectionProperty) {
        StringBuilder sb = new StringBuilder(TYPICAL_URL_LENGTH);

        if (null!=owner) {
            sb.append("/" + owner.getResourceOwnerType().name() + "/" + owner.getId());
        }
        if (null!=collectionType) {
            sb.append("/" + collectionType.getUrlPath());
        }
        if (null!=collectionProperty) {
            sb.append("/" + collectionProperty);
        }

        return sb.toString();
    }


    protected static String nullSafeConstructRelativeUrl(FitbitResourceOwner owner, APICollectionType collectionType, LocalDate date, String subscriptionId) {
    	StringBuilder sb = new StringBuilder(TYPICAL_URL_LENGTH);

    	if (null!=owner) {
    		sb.append("/" + owner.getResourceOwnerType().name() + "/" + owner.getId());
    	}
        if (null!=collectionType) {
            if( null!= subscriptionId ) {
                sb.append("/"+collectionType.getSubscriptionPath());
            } else {
                sb.append("/" + collectionType.getUrlPath());
            }
        }
    	if (null!=date) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            sb.append("/date/" + formatter.print(date));
    	}
    	if (null!=subscriptionId) {
    		sb.append("/apiSubscriptions");
    		if (! subscriptionId.equals(UNSPECIFIED_SUBSCRIPTION_ID)) {
    			sb.append("/" + subscriptionId);
    		}
    	}
    	
    	return sb.toString();
    }

    
    protected static String nullSafeContextualizeUrl(String baseUrl, APIVersion version, String relativeUrl, APIFormat format) {    
    	StringBuilder sb = new StringBuilder(TYPICAL_URL_LENGTH);

    	if (null!=baseUrl) {
    		sb.append(baseUrl);
    	}
    	if (null!=version) {
    		sb.append("/" + version.getVersion());
    	}
    	if (null!=relativeUrl) {
    		sb.append(relativeUrl);
    	}
    	if (null!=format) {
    		sb.append("." + format.toString().toLowerCase());
    	}
    	
    	return sb.toString();
    }

    public static String constructTimeSeriesUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, TimeSeriesResourceType resourceType,
                                                String startDate, String endDateOrPeriod, APIFormat format) {
        return baseUrl + '/' + version.getVersion()
                + '/' + owner.getResourceOwnerType().name() + '/' + owner.getId()
                + resourceType.getResourcePath()
                + "/date/" + startDate
                + '/' + endDateOrPeriod
                + '.' + format.toString().toLowerCase();
    }

    public static String constructTimeSeriesUrl(String baseUrl, APIVersion version, FitbitResourceOwner owner, TimeSeriesResourceType resourceType,
                                                String startDate, String endDateOrPeriod,
                                                String startTime, String endTime, APIFormat format) {
        return baseUrl + '/' + version.getVersion()
                + '/' + owner.getResourceOwnerType().name() + '/' + owner.getId()
                + resourceType.getResourcePath()
                + "/date/" + startDate
                + '/' + endDateOrPeriod
                + "/time/" + startTime
                + '/' + endTime
                + '.' + format.toString().toLowerCase();
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
