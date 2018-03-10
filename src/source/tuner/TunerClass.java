/*******************************************************************************
 *     SDR Trunk 
 *     Copyright (C) 2014-2016 Dennis Sheirer
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
package source.tuner;

import javax.usb.UsbDeviceDescriptor;
import java.util.HashMap;
import java.util.Map;

public enum TunerClass
{
	AIRSPY( TunerType.AIRSPY_R820T, "1D50", "60A1", "Airspy", "Airspy" ),
	GENERIC_2832( TunerType.RTL2832_VARIOUS, "0BDA", "2832", "RTL2832", "SDR" ),        		
	GENERIC_2838( TunerType.RTL2832_VARIOUS, "0BDA", "2838", "RTL2832", "SDR" ),               
	COMPRO_VIDEOMATE_U620F( TunerType.ELONICS_E4000, "185B", "0620", "Compro", "Videomate U620F" ),   
	COMPRO_VIDEOMATE_U650F( TunerType.ELONICS_E4000, "185B", "0650", "Compro", "Videomate U620F" ),   
	COMPRO_VIDEOMATE_U680F( TunerType.ELONICS_E4000, "185B", "0680", "Compro", "Videomate U620F" ),   
	DEXATEK_LOGILINK_VG002A( TunerType.FCI_FC2580, "1D19", "1101", "Dexatek", "Logilink VG0002A" ),
	DEXATEK_DIGIVOX_MINI_II_REV3( TunerType.FCI_FC2580, "1D19", "1102", "Dexatek", "MSI Digivox Mini II v3.0" ),
	DEXATEK_5217_DVBT( TunerType.FCI_FC2580, "1D19", "1103", "Dexatek", "5217 DVB-T" ),
	ETTUS_USRP_B100( TunerType.ETTUS_VARIOUS, "2500", "0002", "Ettus Research", "USRP B100" ),
	FUNCUBE_DONGLE_PRO( TunerType.FUNCUBE_DONGLE_PRO, "04D8", "FB56", "Hamlincrest", "Funcube Dongle Pro" ),
	FUNCUBE_DONGLE_PRO_PLUS( TunerType.FUNCUBE_DONGLE_PRO_PLUS, "04D8", "FB31", "Hamlincrest", "Funcube Dongle Pro Plus" ),
	GIGABYTE_GTU7300( TunerType.FITIPOWER_FC0012, "1B80", "D393", "Gigabyte", "GT-U7300" ),
	GTEK_T803( TunerType.FITIPOWER_FC0012, "1F4D", "B803", "GTek", "T803" ),
	HACKRF_ONE( TunerType.HACKRF, "1D50", "6089", "Great Scott Gadgets", "HackRF One" ),
	RAD1O( TunerType.HACKRF, "1D50", "CC15", "Munich hackerspace", "Rad1o" ),
	LIFEVIEW_LV5T_DELUXE( TunerType.FITIPOWER_FC0012, "1F4D", "C803", "Liveview", "LV5T Deluxe" ),
	LIME_SDR(TunerType.LIMESDR, "1D50", "6108", "LimeSDR", "LimeSDR"),
	MYGICA_TD312( TunerType.FITIPOWER_FC0012, "1F4D", "D286", "MyGica", "TD312" ),
	PEAK_102569AGPK( TunerType.FITIPOWER_FC0012, "1B80", "D395", "Peak", "102569AGPK" ),
	PROLECTRIX_DV107669( TunerType.FITIPOWER_FC0012, "1F4D", "D803", "Prolectrix", "DV107669" ),
	SVEON_STV20( TunerType.FITIPOWER_FC0012, "1B80", "D39D", "Sveon", "STV20 DVB-T USB & FM" ),
	TERRATEC_CINERGY_T_REV1( TunerType.FITIPOWER_FC0012, "0CCD", "00A9", "Terratec", "Cinergy T R1" ),
	TERRATEC_CINERGY_T_REV3( TunerType.ELONICS_E4000, "0CCD", "00D3", "Terratec", "Cinergy T R3" ),
	TERRATEC_NOXON_REV1_B3( TunerType.FITIPOWER_FC0013, "0CCD", "00B3", "Terratec", "NOXON R1 (B3)" ),
	TERRATEC_NOXON_REV1_B4( TunerType.FITIPOWER_FC0013, "0CCD", "00B4", "Terratec", "NOXON R1 (B4)" ),
	TERRATEC_NOXON_REV1_B7( TunerType.FITIPOWER_FC0013, "0CCD", "00B7", "Terratec", "NOXON R1 (B7)" ),
	TERRATEC_NOXON_REV1_C6( TunerType.FITIPOWER_FC0013, "0CCD", "00C6", "Terratec", "NOXON R1 (C6)" ),
	TERRATEC_NOXON_REV2( TunerType.ELONICS_E4000, "0CCD", "00E0", "Terratec", "NOXON R2" ),
	TERRATEC_T_STICK_PLUS( TunerType.ELONICS_E4000, "0CCD", "00D7", "Terratec", "T Stick Plus" ),
	TWINTECH_UT40( TunerType.FITIPOWER_FC0013, "1B80", "D3A4", "Twintech", "UT-40" ),
	ZAAPA_ZTMINDVBZP( TunerType.FITIPOWER_FC0012, "1B80", "D398", "Zaapa", "ZT-MINDVBZP" ), 
	UNKNOWN( TunerType.UNKNOWN, "0000", "0000", "Unknown Manufacturer", "Unknown Device" );

	private static Map<String, Map<String, TunerClass>> deviceMap = initializeMapping();

	private TunerType mTunerType;
	private String mVendorID;
	private String mDeviceID;
	private String mVendorDescription;
	private String mDeviceDescription;
	
	TunerClass( TunerType tunerType,
					   String vendorID, 
					   String deviceID,
					   String vendorDescription,
					   String deviceDescription )
	{
		mTunerType = tunerType;
		mVendorID = vendorID;
		mDeviceID = deviceID;
		mVendorDescription = vendorDescription;
		mDeviceDescription = deviceDescription;
	}

    private static Map<String, Map<String, TunerClass>> initializeMapping() {
        Map<String, Map<String, TunerClass>> retMap = new HashMap<>();
        for (TunerClass c : TunerClass.values()) {
            if (retMap.containsKey(c.mVendorID)) {
                retMap.get(c.mVendorID).put(c.mDeviceID, c);
            } else {
                Map<String, TunerClass> tmp = new HashMap<>();
                tmp.put(c.mDeviceID, c);
                retMap.put(c.mVendorID, tmp);
            }
        }
        return retMap;
    }

	public String toString()
	{
		return "USB" +
				" Tuner:" + mTunerType.toString() +
				" Vendor:" + mVendorDescription + 
				" Device:" + mDeviceDescription +
				" Address:" + mVendorID + ":" + mDeviceID;
	}
	
	public String getVendorDeviceLabel()
	{
		return mVendorDescription + " " + mDeviceDescription;
	}
	
	public TunerType getTunerType()
	{
		return mTunerType;
	}
	
	public static TunerClass valueOf( UsbDeviceDescriptor descriptor )
	{
		return valueOf( descriptor.idVendor(), descriptor.idProduct() );
	}

	public static TunerClass valueOf(short vendor, short product) {
		TunerClass retVal = null;
		//Cast the short to integer so that we can switch on unsigned numbers
		String vendorID = String.format("%04x", vendor & 0xFFFF).toUpperCase();
		String productID = String.format("%04x", product & 0xFFFF).toUpperCase();
		//Look up vendor / product in device map, if found return corresponding TunerClass
		if (deviceMap.containsKey(vendorID)) {
			retVal = deviceMap.get(vendorID).get(productID);
		}
		return (retVal == null) ? TunerClass.UNKNOWN : retVal;
	}

	public String getVendorID()
	{
		return mVendorID;
	}
	
	public String getDeviceID()
	{
		return mDeviceID;
	}
	
	public String getVendorDescription()
	{
		return mVendorDescription;
	}
	
	public String getDeviceDescription()
	{
		return mDeviceDescription;
	}
}
