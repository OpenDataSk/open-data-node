package sk.opendata.odn.repository;


/**
 * This is a Open Data Node Repository interface defining "internal API"
 * between "serialization" and "repository" classes.
 *
 * @param <RecordType> type of records which are going to be stored in repository
 */
public interface OdnRepositoryInterface<RecordType> {
	
	/**
	 * Store given record into the back-end with given name.
	 * 
	 * Essentially, a repository can use multiple storages/back-ends so the
	 * {@code name} defines which one to store into.
	 * 
	 * @param name
	 *            name of the store/back-end to store into
	 * @param record
	 *            record to store
	 * @param contexts
	 *            the context for RDF statements used for the statements in the
	 *            repository
	 * 
	 * @throws IllegalArgumentException
	 *             when some of the given arguments is not valid
	 * @throws OdnRepositoryException
	 *             when storage operation fails
	 */
	public void store(String name, RecordType record, String... contexts)
			throws IllegalArgumentException, OdnRepositoryException;

}
