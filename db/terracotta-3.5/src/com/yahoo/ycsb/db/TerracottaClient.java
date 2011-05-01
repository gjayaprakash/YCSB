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
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

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
     */
    @Override
    public void init() throws DBException {
        super.init();
        cacheManager = new CacheManager();
    }

    /**
     * Shutdown the CacheManager instance.
     */
    @Override
    public void cleanup() throws DBException {
        cacheManager.shutdown();

        super.cleanup();
    }

    /**
     * Read is implemented as a simple Cache.get()
     * 
     * @see com.yahoo.ycsb.DB#read(String, String, Set, HashMap)
     */
    @Override
    public int read(String table, String key, Set<String> fields,
            HashMap<String, String> result) {
        Ehcache cache = cacheManager.getEhcache(table);
        Element elem = cache.get(key);
        if (elem == null) {
            return ERROR;
        }
        Record record = (Record) elem.getValue();
        Map<String, String> retrievedMap = record.value();
        Set<String> recordFields = (fields != null) ? fields : retrievedMap
                .keySet();
        for (String field : recordFields) {
            result.put(field, retrievedMap.get(field));
        }

        return SUCCESS;
    }

    /**
     * Scan is implemented by using the Ehcache Search API
     * 
     * The scan operation will use a search for all elements with a "count"
     * value between the count value of the startKey and (startKey +
     * recordcount).
     * 
     * @see com.yahoo.ycsb.DB#scan(java.lang.String, java.lang.String, int,
     *      java.util.Set, java.util.Vector)
     */
    @Override
    public int scan(String table, String startkey, int recordcount,
            Set<String> fields, Vector<HashMap<String, String>> result) {

        Ehcache cache = cacheManager.getEhcache(table);
        if (!cache.isSearchable()) {
            LOG.error("Scan not implemented for caches that aren't searchable. See README.");
            return ERROR;
        }
        Long startRecord = ((Record) cache.get(startkey).getValue()).count();
        Attribute<Long> count = cache.getSearchAttribute("count");
        Results results = cache
        .createQuery()
        .includeValues()
        .addCriteria(
                count.between(startRecord, startRecord + recordcount))
                .execute();
        for (Result r : results.all()) {
            result.add(new HashMap<String, String>(((Record) r.getValue())
                    .value()));
        }
        return SUCCESS;
    }

    /**
     * Updates and inserts are the same for an Ehcache
     * 
     * @see com.yahoo.ycsb.DB#update(java.lang.String, java.lang.String,
     *      java.util.HashMap)
     */
    @Override
    public int update(String table, String key, HashMap<String, String> values) {
        return insert(table, key, values);
    }

    /**
     * The insert operation is implemented by doing a Cache.put
     * 
     * The values HashMap is stored with an entry count in a Record object
     * that's put into the cache.
     * 
     * @see com.yahoo.ycsb.DB#insert(java.lang.String, java.lang.String,
     *      java.util.HashMap)
     */
    @Override
    public int insert(String table, String key, HashMap<String, String> values) {
        Ehcache cache = cacheManager.getEhcache(table);
        Record r = new Record(Long.parseLong(key.replaceFirst("user", "")),
                values);
        Element elem = new Element(key, r);
        cache.put(elem);

        return SUCCESS;
    }

    /**
     * Delete is a Cache.remove()
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
