/*
 * Copyright 2017 Terencio Agozzino
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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

/**
 * A base model class for data block.
 *
 * @author Terencio Agozzino
 */
public class DataBlock implements Parcelable {

    /**
     * Database field names.
     */
    public static final String DB_FIELD_NAME = "db";
    public static final String ID_FIELD_NAME = "id";
    public static final String OFFSET_FIELD_NAME = "offset";
    public static final String AMOUNT_FIELD_NAME= "amount";
    public static final String DATA_FIELD_NAME = "data";

    /**
     * Properties with ORMLite annotations for the database.
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    @DatabaseField(columnName = DB_FIELD_NAME, canBeNull = false)
    private int dbNumber;

    @DatabaseField(columnName = OFFSET_FIELD_NAME, canBeNull = false)
    private int offset;

    @DatabaseField(columnName = AMOUNT_FIELD_NAME, canBeNull = false)
    private int amount;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = DATA_FIELD_NAME,
            canBeNull = false)
    private byte[] data;

    /**
     * Default constructor of the DataBlock class needed for ORMLite.
     */
    public DataBlock() { }

    /**
     * Main constructor of the DataBlock class.
     *
     * @param dbNumber the number of the data block
     * @param offset the offset at the beginning of the data block
     * @param amount the amount of words of the data block
     * @param data the data storage area of the data block
     */
    public DataBlock(int dbNumber, int offset, int amount, byte[] data) {
        this.dbNumber = dbNumber;
        this.offset = offset;
        this.amount = amount;
        this.data = data;
    }

    /**
     * Retrieves PLC data from Parcel object.
     *
     * This constructor is called from from the method
     * createFromParcel(Parcel parcel) of the CREATOR object.
     *
     * @param source the Parcel containing the PLC data
     */
    private DataBlock(Parcel source) {
        this.id = source.readInt();
        this.dbNumber = source.readInt();
        this.offset = source.readInt();
        this.amount = source.readInt();
        this.data = new byte[source.readInt()];
        source.readByteArray(this.data);
    }

    /**
     * Gets the amount of words of the data block.
     *
     * @return the amount of words of the data block
     */
    public int getAmount() { return amount; }

    /**
     * Sets the amount of words of the data block.
     *
     * @param amount the amount of words of the data block
     */
    public void setAmount(int amount) { this.amount = amount; }

    /**
     * Gets the data storage area of the data block.
     *
     * @return the data storage area of the data block
     */
    public byte[] getData() { return data; }

    /**
     * Sets the data storage area of the data block.
     *
     * @param data the data storage area of the data block
     */
    public void setData(byte[] data) { this.data = data; }

    /**
     * Gets the number of the data block.
     *
     * @return the number of the data block
     */
    public int getDbNumber() { return dbNumber; }

    /**
     * Sets the number of the data block.
     *
     * @param dbNumber the number of the data block
     */
    public void setDbNumber(int dbNumber) { this.dbNumber = dbNumber; }

    /**
     * Gets the offset at the beginning of the data block.
     *
     * @return the offset at the beginning of the data block
     */
    public int getOffset() { return offset; }

    /**
     * Sets the offset at the beginning of the data block.
     *
     * @param offset the offset at the beginning of the data block
     */
    public void setOffset(int offset) { this.offset = offset; }

    @Override
    public String toString() {
        return "Data Block{" +
                "id=" + id + '\'' +
                "dbNumber=" + dbNumber + '\'' +
                ", offset='" + offset + '\'' +
                ", amount='" + amount + '\'' +
                ", data=" + data + '\'' +
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
        dest.writeInt(dbNumber);
        dest.writeInt(offset);
        dest.writeInt(amount);
        dest.writeByteArray(data);
    }

    /**
     * Generates instances of your Parcelable class from a Parcel.
     */
    public static final Parcelable.Creator<DataBlock> CREATOR = new Parcelable.Creator<DataBlock>() {

        /**
         * Creates a new instance of the Plc class, instantiating it from the
         * given Parcel whose data had previously been written by.
         *
         * @param source the Parcel to read the object's data from
         * @return a new instance of the Plc class.
         */
        @Override
        public DataBlock createFromParcel(Parcel source) { return new DataBlock(source); }

        /**
         * Creates a new array of the Plc class.

         * @param size the size of the array.
         * @return an array of the Plc class, with every entry initialized to
         *         null.
         */
        @Override
        public DataBlock[] newArray(int size) { return new DataBlock[size]; }
    };
}