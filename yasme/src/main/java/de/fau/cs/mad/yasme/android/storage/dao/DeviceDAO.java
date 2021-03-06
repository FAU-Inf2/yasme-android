package de.fau.cs.mad.yasme.android.storage.dao;

import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.User;

import java.util.List;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 19.07.14.
 */
public interface DeviceDAO {

    /**
     * Creates a device item but only if the ID does not already exist in the table. This
     * extracts the id from the parameter, queries for it's id, returning the data if it
     * exists. If it does not exist create will be called with the parameter.
     * @param device to be added
     * @return given device or null if an error occurs
     */
    public Device addIfNotExists(Device device);


    /**
     * This method is a combination of addIfNotExists and update for the sake of convenience.
     * Queries for a device item with the specified id. If there is none, addIfNotExists is called,
     * otherwise update.
     * @param device to be added or updated
     * @return given chat after insertion / update
     */
    public Device addOrUpdate(Device device);

    /**
     * Retrieves an object associated with a specific ID.
     * @param id Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     */
    public Device get(long id);

    /**
     * Query for all of the items in the object table
     * @return list of all devices or null on error
     */
    public List<Device> getAll();

    public List<Device> getAll(User user);



    /**
     * Store the fields from an object to the database row corresponding to the id from the
     * data parameter. If you have made changes to an object, this is how you persist those
     * changes to the database. You cannot use this method to update the id field
     * @param device to be updated
     * @return given device or null on error
     */
    public Device update(Device device);


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * @param device to be deleted
     * @return true on success, false otherwise
     */
    public boolean delete(Device device);

    public boolean deleteAll(User user);

}
