/*******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2017 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 ******************************************************************************/
package source.tuner.limesdr;

import source.tuner.TunerType;
import source.tuner.configuration.TunerConfiguration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by billh on 3/7/17.
 */
public class LimeSDRTunerConfiguration extends TunerConfiguration {

    public enum SampleRates {
        MHZ_1(1000000, "1 MHZ"), MHZ_2_5(2500000, "2.5 MHZ"), MHZ_5(5000000, "5 MHZ"), MHZ_10(10000000,
                "10 MHZ"), MHZ_15(15000000, "15 MHZ"), MHZ_20(20000000, "20 MHZ"), MHZ_25(25000000,
                "25 MHZ"), MHZ_30(30000000, "30 MHZ"), MHZ_40(40000000, "40 MHZ"), MHZ_45(45000000,
                "45 MHZ"), MHZ_50(50000000, "50 MHZ"), MHZ_55(55000000, "55 MHZ");

        private final int rateInHz;
        private final String display;
        private static Map<Integer, SampleRates> lookup = new HashMap<>();

        static {
            for (SampleRates r : SampleRates.values()) {
                lookup.put(r.getRateInSamplesPerSecond(), r);
            }
        }

        SampleRates(int rateInHz, String display) {
            this.rateInHz = rateInHz;
            this.display = display;
        }

        public static SampleRates getRate(int value) {
            SampleRates ret = lookup.get(value);
            return (ret == null) ? SampleRates.MHZ_10 : ret;
        }

        @Override
        public String toString() {
            return display;
        }

        public int getRateInSamplesPerSecond() {
            return rateInHz;
        }
    }

    @XmlTransient
    static final Integer[] LNAGAIN = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
    @XmlTransient
    static final Integer[] PGAGAIN = new Integer[]{-12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    @XmlTransient
    static final Integer[] TIAGAIN = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    @XmlTransient
    static final String[] ANTENNAS = new String[]{"NONE", "LNAH", "LNAL", "LNAW", "LB1", "LB2"};
    @XmlTransient
    static final Long[] BUFFER_SIZE = new Long[]{10000l, 25000l, 50000l, 75000l, 100000l, 125000l, 150000l};
    @XmlTransient
    static final String[] CLOCK_SOURCES = new String[]{"internal"};


    private String mClockSource = CLOCK_SOURCES[0];
    private String mRxAntenna = ANTENNAS[0];
    private SampleRates mRxSampleRate = SampleRates.MHZ_10;
    private long mRxBufferSize = BUFFER_SIZE[4];
    private double mRxGainLNA = 0.0;
    private double mRxGainTIA = 0.0;
    private double mRxGainPGA = 0.0;

    /**
     * Default constructor for JAXB
     */
    public LimeSDRTunerConfiguration() {
    }

    public LimeSDRTunerConfiguration(String uniqueID, String name) {
        super(uniqueID, name);
    }

    @Override
    public TunerType getTunerType() {
        return TunerType.LIMESDR;
    }

    @XmlAttribute(name = "clock_source")
    public String getClockSource() {
        return mClockSource;
    }

    public void setClockSource(String mClockSource) {
        this.mClockSource = mClockSource;
    }

    @XmlAttribute(name = "rx_sample_rate")
    public long getRxSampleRate() {
        return mRxSampleRate.getRateInSamplesPerSecond();
    }

    public void setRxSampleRate(long mRxSampleRate) {
        this.mRxSampleRate = LimeSDRTunerConfiguration.SampleRates.getRate(Math.toIntExact(mRxSampleRate));
    }

    @XmlAttribute(name = "rx_gain_pga")
    public double getRxGainPGA() {
        return this.mRxGainPGA;
    }

    public void setRxGainPGA(double mRxGainPGA) {
        this.mRxGainPGA = mRxGainPGA;
    }

    @XmlAttribute(name = "rx_gain_lna")
    public double getRxGainLNA() {
        return mRxGainLNA;
    }

    public void setRxGainLNA(double mRxGainLNA) {
        this.mRxGainLNA = mRxGainLNA;
    }

    @XmlAttribute(name = "rx_gain_tia")
    public double getRxGainTIA() {
        return mRxGainTIA;
    }

    public void setRxGainTIA(double mRxGainTIA) {
        this.mRxGainTIA = mRxGainTIA;
    }

    @XmlAttribute(name = "rx_antenna")
    public String getRxAntenna() {
        return mRxAntenna;
    }

    public void setRxAntenna(String mRxAntenna) {
        this.mRxAntenna = mRxAntenna;
    }

    @XmlAttribute(name = "rx_buffer_size")
    public long getRxBufferSize() {
        return mRxBufferSize;
    }

    public void setRxBufferSize(long mRxBufferSize) {
        this.mRxBufferSize = mRxBufferSize;
    }
}