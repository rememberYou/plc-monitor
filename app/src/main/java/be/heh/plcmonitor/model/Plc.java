/*
 * Copyright 2017 the original Terencio Agozzino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.heh.plcmonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

/**
 * A base model class for user.
 *
 * This class uses Parcelable instead of Serializable to avoid the slow process
 * of reflection that also create a bunch of temporary objects and cause quite
 * a bit of garbage collection.
 *
 * @author Terencio Agozzino
 */
public class Plc implements Parcelable {

    /**
     * Database field names.
     */
    public static final String ID_FIELD_NAME = "id";
    public static final String IP_FIELD_NAME = "ip";
    public static final String NAME_FIELD_NAME = "name";
    public static final String RACK_FIELD_NAME= "rack";
    public static final String SLOT_FIELD_NAME = "slot";
    public static final String DATA_BLOCK_ID_FIELD_NAME = "data_block_id";

    /**
     * Properties with ORMLite annotations for the database.
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    private String name;

    @DatabaseField(columnName = IP_FIELD_NAME, canBeNull = false)
    private String ip;

    @DatabaseField(columnName = RACK_FIELD_NAME, canBeNull = false)
    private int rack;

    @DatabaseField(columnName = SLOT_FIELD_NAME, canBeNull = false)
    private int slot;

    @DatabaseField(foreign = true, columnName = DATA_BLOCK_ID_FIELD_NAME,
            index = true, foreignAutoRefresh = true)
    private DataBlock dataBlock;

    /**
     * Default constructor of the Plc class needed for ORMLite.
     */
    public Plc() { }

    /**
     * Main constructor of the Plc class.
     *
     * @param name the name of the PLC
     * @param ip the IP address of the PLC
     * @param rack the rack of the PLC
     * @param slot the slot of the PLC
     * @param dataBlock the data block of the PLC
     */
    public Plc(String name, String ip, int rack, int slot,
               DataBlock dataBlock) {
        this.name = name;
        this.ip = ip;
        this.rack = rack;
        this.slot = slot;
        if (dataBlock != null) {
            this.dataBlock = dataBlock;
        }
    }

    /**
     * Retrieves PLC data from Parcel object.
     *
     * This constructor is called from from the method
     * createFromParcel(Parcel parcel) of the CREATOR object.
     *
     * @param source the Parcel containing the PLC data
     */
    private Plc(Parcel source) {
        this.id = source.readInt();
        this.name = source.readString();
        this.ip = source.readString();
        this.rack = source.readInt();
        this.slot = source.readInt();
        this.dataBlock = (DataBlock) source.readValue(DataBlock.class.getClassLoader());
    }

    /**
     * Gets the data block of the PLC.
     *
     * @return the data block of the PLC
     */
    public DataBlock getDataBlock() { return dataBlock; }

    /**
     * Sets the data block of the PLC.
     *
     * @param dataBlock the data block of the PLC
     */
    public void setDataBlock(DataBlock dataBlock) { this.dataBlock = dataBlock; }

    /**
     * Gets the identifier of the PLC.
     *
     * @return the identifier of the PLC
     */
    public int getId() { return id; }

    /**
     * Sets the identifier of the PLC.
     *
     * @param id the identifier of the PLC
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the IP address of the PLC.
     *
     * @return the IP address of the PLC
     */
    public String getIp() { return ip; }

    /**
     * Sets the IP address of the PLC.
     *
     * @param ip the IP address of the PLC
     */
    public void setIp(String ip) { this.ip = ip; }

    /**
     * Gets the name of the PLC.
     *
     * @return the name of the PLC
     */
    public String getName() { return name; }

    /**
     * Sets the name of the PLC.
     *
     * @param name the name of the PLC
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the rack of the PLC.
     *
     * @return the rack of the PLC
     */
    public int getRack() { return rack; }

    /**
     * Sets the rack of the PLC.
     *
     * @param rack the rack of the PLC
     */
    public void setRack(int rack) { this.rack = rack; }

    /**
     * Gets the slot of the PLC.
     *
     * @return the slot of the PLC
     */
    public int getSlot() { return slot; }

    /**
     * Sets the slot of the PLC.
     *
     * @param slot the slot of the PLC
     */
    public void setSlot(int slot) { this.slot = slot; }

    /**
     * Specifies whether two PLCs are the same.
     *
     * @param plc the other PLC
     * @return true if the identifier of the PLCs are the same; false otherwise
     */
    public boolean equals(Plc plc) { return this.id == plc.id; }

    /**
     * Specifies the representation of the PLC.
     *
     * @return the details of the PLC
     */
    @Override
    public String toString() {
        return "Plc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", rack=" + rack + '\'' +
                ", slot=" + slot + '\'' +
                ", dataBlock=" + dataBlock + '\'' +
                '}';
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of writeToParcel(Parcel, int),
     * the return value of this method must include the CONTENTS_FILE_DESCRIPTOR
     * bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     *         by this Parcelable object instance.
     *         Value is either 0 or CONTENTS_FILE_DESCRIPTOR.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest the Parcel in which the object should be written.
     * @param flags additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     *              Value is either 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeInt(rack);
        dest.writeInt(slot);
        dest.writeParcelable(dataBlock, flags);
    }

    /**
     * Generates instances of your Parcelable class from a Parcel.
     */
    public static final Parcelable.Creator<Plc> CREATOR = new Parcelable.Creator<Plc>() {

        /**
         * Creates a new instance of the Plc class, instantiating it from the
         * given Parcel whose data had previously been written by.
         *
         * @param source the Parcel to read the object's data from
         * @return a new instance of the Plc class.
         */
        @Override
        public Plc createFromParcel(Parcel source) { return new Plc(source); }

        /**
         * Creates a new array of the Plc class.

         * @param size the size of the array.
         * @return an array of the Plc class, with every entry initialized to
         *         null.
         */
        @Override
        public Plc[] newArray(int size) { return new Plc[size]; }
    };
}