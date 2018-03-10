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

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.SourceException;
import source.tuner.configuration.TunerConfiguration;
import source.tuner.configuration.TunerConfigurationEditor;
import source.tuner.configuration.TunerConfigurationEvent;
import source.tuner.configuration.TunerConfigurationModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by billh on 3/9/17.
 */
public class LimeSDRTunerEditor extends TunerConfigurationEditor {
    private static final long serialVersionUID = 1L;

    private final static Logger mLog = LoggerFactory.getLogger(LimeSDRTunerEditor.class);

    private JTextField mConfigurationName;
    private JButton mTunerInfo;
    private JComboBox<String> mComboAntenna;
    private JComboBox<String> mComboClockSource;
    private JComboBox<LimeSDRTunerConfiguration.SampleRates> mComboSampleRate;
    private JComboBox<Long> mComboBufferSize;
    private JComboBox<Integer> mComboLNAGain;
    private JComboBox<Integer> mComboPGAGain;
    private JComboBox<Integer> mComboTIAGain;
    private boolean mLoading;

    private LimeSDRTunerController mController;

    public LimeSDRTunerEditor(TunerConfigurationModel tunerConfigurationModel, LimeSDRTuner tuner) {
        super(tunerConfigurationModel);

        mController = tuner.getController();

        init();
    }

    private LimeSDRTunerConfiguration getConfiguration() {
        if (hasItem()) {
            return (LimeSDRTunerConfiguration) getItem();
        }

        return null;
    }

    private void init() {
        setLayout(new MigLayout("fill,wrap 4", "[right][grow,fill][right][grow,fill]", "[][][][][][][][][][][grow]"));

        add(new JLabel("LimeSDR Tuner Configuration"), "span,align center");

        mConfigurationName = new JTextField();
        mConfigurationName.setEnabled(false);
        mConfigurationName.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                save();
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });

        add(new JLabel("Name:"));
        add(mConfigurationName, "span 2");

        mTunerInfo = new JButton("Tuner Info");
        mTunerInfo.setEnabled(false);
        mTunerInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, getTunerInfo(), "Tuner Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(mTunerInfo);

        add(new JSeparator(JSeparator.HORIZONTAL), "span,grow");

        mComboClockSource = new JComboBox<>(LimeSDRTunerConfiguration.CLOCK_SOURCES);
        mComboClockSource.setEnabled(false);
        mComboClockSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String source = (String) mComboClockSource.getSelectedItem();
                try {
                    mController.setClockSource(source);
                    save();
                } catch (SourceException e2) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, "Lime Tuner Controller"
                            + " - could not set clock source setting [" + source + "] " + e2.getLocalizedMessage());
                    mLog.error("Lime Tuner Controller - couldn't set clock source selection [" + source + "]", e);
                }
            }
        });
        add(new JLabel("Clock Source:"));
        add(mComboClockSource);

        mComboAntenna = new JComboBox<>(LimeSDRTunerConfiguration.ANTENNAS);
        mComboAntenna.setEnabled(true);
        mComboAntenna.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String antenna = (String) mComboAntenna.getSelectedItem();
                try {
                    mController.setAntenna(antenna);
                    save();
                } catch (SourceException e2) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, "Lime Tuner Controller"
                            + " - could not set antenna setting [" + antenna + "] " + e2.getLocalizedMessage());
                    mLog.error("Lime Tuner Controller - couldn't set antenna selection [" + antenna + "]", e);
                }
            }
        });
        add(new JLabel("Antenna:"));
        add(mComboAntenna);

        mComboSampleRate = new JComboBox<>(LimeSDRTunerConfiguration.SampleRates.values());
        mComboSampleRate.setEnabled(false);
        mComboSampleRate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LimeSDRTunerConfiguration.SampleRates sampleRate = ((LimeSDRTunerConfiguration.SampleRates) mComboSampleRate
                        .getSelectedItem());
                try {
                    mController.setSampleRate(sampleRate.getRateInSamplesPerSecond());
                    save();
                } catch (SourceException e2) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this,
                            "Lime Tuner Controller" + " - couldn't apply the sample rate setting ["
                                    + sampleRate.getRateInSamplesPerSecond() + "] " + e2.getLocalizedMessage());
                    mLog.error("Lime Tuner Controller - couldn't apply sample rate setting ["
                            + sampleRate.getRateInSamplesPerSecond() + "]", e);
                }
            }
        });
        add(new JLabel("Sample Rate:"));
        add(mComboSampleRate);

        mComboBufferSize = new JComboBox<>(LimeSDRTunerConfiguration.BUFFER_SIZE);
        mComboBufferSize.setEnabled(true);
        mComboBufferSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long bufferSize = (Long) mComboBufferSize.getSelectedItem();
                mController.setBufferSize(bufferSize);
                save();
            }
        });
        add(new JLabel("Buffer Size:"));
        add(mComboBufferSize);

        add(new JSeparator(JSeparator.HORIZONTAL), "span,grow");
        add(new JLabel("Gain"), "wrap");

        mComboLNAGain = new JComboBox<Integer>(LimeSDRTunerConfiguration.LNAGAIN);
        mComboLNAGain.setEnabled(false);
        mComboLNAGain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    int lnaGain = (Integer) mComboLNAGain.getSelectedItem();
                    mController.setLNAGain(lnaGain);
                    save();
                } catch (RuntimeException | SourceException e) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, "LimeSDR Tuner Controller"
                            + " - couldn't apply the LNA gain setting - " + e.getLocalizedMessage());
                    mLog.error("LimeSDR Tuner Controller - couldn't apply LNA gain setting - ", e);
                }
            }
        });
        mComboLNAGain.setToolTipText("<html>LNA Gain.  Adjust to set the IF gain</html>");
        add(new JLabel("LNA Gain: "));
        add(mComboLNAGain);

        mComboPGAGain = new JComboBox<Integer>(LimeSDRTunerConfiguration.PGAGAIN);
        mComboPGAGain.setEnabled(false);
        mComboPGAGain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    int pgaGain = (Integer) mComboPGAGain.getSelectedItem();
                    mController.setPGAGain(pgaGain);
                    save();
                } catch (RuntimeException | SourceException e) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, "LimeSDR Tuner Controller"
                            + " - couldn't apply the PGA gain setting - " + e.getLocalizedMessage());

                    mLog.error("LimeSDR Tuner Controller - couldn't apply PGA gain setting - ", e);
                }
            }
        });
        mComboPGAGain.setToolTipText("<html>PGA Gain.  Adjust to set the IF gain</html>");
        add(new JLabel("PGA Gain"));
        add(mComboPGAGain);

        mComboTIAGain = new JComboBox<Integer>(LimeSDRTunerConfiguration.TIAGAIN);
        mComboTIAGain.setEnabled(false);
        mComboTIAGain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    int pgaGain = (Integer) mComboTIAGain.getSelectedItem();
                    mController.setPGAGain(pgaGain);
                    save();
                } catch (RuntimeException | SourceException e) {
                    JOptionPane.showMessageDialog(LimeSDRTunerEditor.this, "LimeSDR Tuner Controller"
                            + " - couldn't apply the TIA gain setting - " + e.getLocalizedMessage());

                    mLog.error("LimeSDR Tuner Controller - couldn't apply TIA gain setting - ", e);
                }
            }
        });
        mComboTIAGain.setToolTipText("<html>TIA Gain.  Adjust to set the IF gain</html>");
        add(new JLabel("TIA Gain: "));
        add(mComboTIAGain);
    }

    /**
     * Sets each of the tuner configuration controls to the enabled argument
     * state
     */
    private void setControlsEnabled(boolean enabled) {
        if (mConfigurationName.isEnabled() != enabled) {
            mConfigurationName.setEnabled(enabled);
        }

        if (mTunerInfo.isEnabled() != enabled) {
            mTunerInfo.setEnabled(enabled);
        }

        if (mComboAntenna.isEnabled() != enabled) {
            mComboAntenna.setEnabled(enabled);
        }

        if (mComboBufferSize.isEnabled() != enabled) {
            mComboBufferSize.setEnabled(enabled);
        }

        if (mComboSampleRate.isEnabled() != enabled) {
            mComboSampleRate.setEnabled(enabled);
        }

        if (mComboLNAGain.isEnabled() != enabled) {
            mComboLNAGain.setEnabled(enabled);
        }

        if (mComboPGAGain.isEnabled() != enabled) {
            mComboPGAGain.setEnabled(enabled);
        }

        if (mComboTIAGain.isEnabled() != enabled) {
            mComboTIAGain.setEnabled(enabled);
        }
    }

    private String getTunerInfo() {
        String board = mController.toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3>LimeSDR Tuner</h3>");
        for (String s : board.split(System.lineSeparator())) {
            sb.append(s + "<br>");
        }
        sb.append("<br>");
        return sb.toString();
    }

    @Override
    public void setItem(TunerConfiguration tunerConfiguration) {
        super.setItem(tunerConfiguration);

        // Toggle loading so that we don't fire a change event and schedule a
        // settings file save
        mLoading = true;

        if (hasItem()) {
            LimeSDRTunerConfiguration config = getConfiguration();
            if (config != null) {
                if (tunerConfiguration.isAssigned()) {
                    setControlsEnabled(true);
                    mConfigurationName.setText(config.getName());
                    mComboClockSource.setSelectedItem(config.getClockSource());
                    mComboAntenna.setSelectedItem(config.getRxAntenna());
                    mComboSampleRate.setSelectedItem(
                            LimeSDRTunerConfiguration.SampleRates.getRate(Math.toIntExact(config.getRxSampleRate())));
                    mComboBufferSize.setSelectedItem(new Long(config.getRxBufferSize()));
                    mComboLNAGain.setSelectedItem(new Double(config.getRxGainLNA()).intValue());
                    mComboPGAGain.setSelectedItem(new Double(config.getRxGainPGA()).intValue());
                    mComboTIAGain.setSelectedItem(new Double(config.getRxGainTIA()).intValue());
                } else {
                    setControlsEnabled(false);
                    mConfigurationName.setText(config.getName());
                }
            }
        } else {
            setControlsEnabled(false);
            mConfigurationName.setText("");
        }

        mLoading = false;
    }

    @Override
    public void save() {
        if (hasItem() && !mLoading) {
            LimeSDRTunerConfiguration config = getConfiguration();
            if(config != null) {
                config.setName(mConfigurationName.getText());
                config.setClockSource((String) mComboClockSource.getSelectedItem());
                config.setRxAntenna((String) mComboAntenna.getSelectedItem());
                config.setRxSampleRate(((LimeSDRTunerConfiguration.SampleRates) mComboSampleRate.getSelectedItem())
                        .getRateInSamplesPerSecond());
                config.setRxBufferSize((Long) mComboBufferSize.getSelectedItem());
                config.setRxGainLNA(((Integer) mComboLNAGain.getSelectedItem()).doubleValue());
                config.setRxGainPGA(((Integer) mComboPGAGain.getSelectedItem()).doubleValue());
                config.setRxGainTIA(((Integer) mComboTIAGain.getSelectedItem()).doubleValue());
                getTunerConfigurationModel()
                        .broadcast(new TunerConfigurationEvent(getConfiguration(), TunerConfigurationEvent.Event.CHANGE));
            }
        }
    }
}