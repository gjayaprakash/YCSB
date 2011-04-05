/**
 * 
 */
package com.yahoo.ycsb.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;

/**
 * @author gautam
 * 
 */
public class TerracottaClient extends DB {

    private CacheManager cacheManager;
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;
    private static final Logger LOG = LoggerFactory
    .getLogger(TerracottaClient.class);

    /**
     * Initialise the cache manager for storing records.
     * 
     * Terracotta clustered cache is assumed.
     */
    @Override
    public void init() throws DBException {
        super.init();

        cacheManager = new CacheManager();
    }

    @Override
    public void cleanup() throws DBException {
        cacheManager.shutdown();

        super.cleanup();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.ycsb.DB#read(java.lang.String, java.lang.String,
     * java.util.Set, java.util.HashMap)
     */
    @Override
    public int read(String table, String key, Set<String> fields,
            HashMap<String, String> result) {
        Ehcache cache = cacheManager.getEhcache(table);
        Element elem = cache.get(key);
        if (elem == null) {
            return ERROR;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> record = (Map<String, String>) elem.getValue();
        Set<String> recordFields = (fields != null) ? fields : record.keySet();
        for (String field : recordFields) {
            result.put(field, record.get(field));
        }

        return SUCCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.ycsb.DB#scan(java.lang.String, java.lang.String, int,
     * java.util.Set, java.util.Vector)
     */
    @Override
    public int scan(String table, String startkey, int recordcount,
            Set<String> fields, Vector<HashMap<String, String>> result) {
        LOG.warn("Scan not supported by the Terracotta DB Client.");
        return ERROR;
    }

    /*
     * (non-Javadoc) Updates and inserts are the same for an Ehcache
     * 
     * @see com.yahoo.ycsb.DB#update(java.lang.String, java.lang.String,
     * java.util.HashMap)
     */
    @Override
    public int update(String table, String key, HashMap<String, String> values) {
        return insert(table, key, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.ycsb.DB#insert(java.lang.String, java.lang.String,
     * java.util.HashMap)
     */
    @Override
    public int insert(String table, String key, HashMap<String, String> values) {
        Ehcache cache = cacheManager.getEhcache(table);
        Element elem = new Element(key, values);
        cache.put(elem);

        return SUCCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.ycsb.DB#delete(java.lang.String, java.lang.String)
     */
    @Override
    public int delete(String table, String key) {
        Ehcache cache = cacheManager.getEhcache(table);
        cache.remove(key);

        return SUCCESS;
    }

}
