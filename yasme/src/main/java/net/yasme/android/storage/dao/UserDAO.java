package net.yasme.android.storage.dao;

import net.yasme.android.entities.User;

import java.util.List;

/**
 * Created by bene on 10.07.14.
 */
public interface UserDAO extends DAO<User> {

    /**
     * Create a new row in the database from an object. The id field will be modified and set with the corresponding id
     * from the database.
     * @param user to insert
     * @return user object after insertion or null if an error occurs
     */
    public User add(User user);


    /**
     * This is a convenience method to creating a data item but only if the ID does not already exist in the table. This
     * extracts the id from the data parameter, queries for the id, returning the data if it
     * exists. If it does not exist addIfNotExists will be called with the parameter.
     *
     * @param user to be added
     * @return Either the data parameter if it was inserted (now with the ID field set via the create method) or the
     *         data element that existed already in the database.
     */
    public User addIfNotExists(User user);

    /**
     * This is a convenience method for creating an item in the database if it does not exist. The id is extracted from
     * the data parameter and a query-by-id is made on the database. If a row in the database with the same id exists
     * then all of the columns in the database will be updated from the fields in the data parameter. If the id is null
     * (or 0 or some other default value) or doesn't exist in the database then the object will be created in the
     * database. This also means that your data item <i>must</i> have an id field defined.
     * @param user to be inserted
     * @return
     */
    public User addOrUpdate(User user);

    /**
     * Store the fields from an object to the database row corresponding to the id from the data parameter. If you have
     * made changes to an object, this is how you persist those changes to the database. You cannot use this method to
     * update the id field
     * @param user item to be updated
     * @return the given user or null if an error occurs
     */
    public User update(User user);


    /**
     * Retrieves an object associated with a specific id
     * @param id of the user to be found
     * @return user or null if not found
     */
    public User get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of users or null in case an error occurs
     */
    public List<User> getAll();

    /**
     * Retrieves all participants with contactFlag = 1
     * @return list of users or null on error
     */
    public List<User> getContacts();


    /**
     * Reset the contactFlag so that the given user is removed from the contact list
     * @param user whose contact flag is to be reset
     * @return true on success, false otherwise
     */
    public boolean removeFromContacts(User user);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param user to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(User user);
}
