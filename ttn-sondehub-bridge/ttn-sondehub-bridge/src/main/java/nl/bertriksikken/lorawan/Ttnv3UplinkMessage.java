package nl.bertriksikken.lorawan;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.lorawan.LoraWanUplinkMessage.ILoraWanUplink;

/**
 * Representation of the TTNv3 uplink message.<br>
 * <br>
 * This is purely a data structure, so all fields are public for easy access.
 * All sub-structures are contained in this file too.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Ttnv3UplinkMessage implements ILoraWanUplink {

	@JsonProperty("end_device_ids")
	EndDeviceIds endDeviceIds = new EndDeviceIds();

	@JsonProperty("received_at")
	String receivedAt = "";

	@JsonProperty("uplink_message")
	UplinkMessage uplinkMessage = new UplinkMessage();

	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class EndDeviceIds {
		@JsonProperty("device_id")
		String deviceId = "";
		@JsonProperty("dev_eui")
		String devEui = "";
	}
	/**
	 * Configuración del transmisor
	 * @author Rafa
	 * @version 1.0
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class Settings {
		@JsonProperty("frequency")
		double frequency;
	}
	/**
	 * Se incluyen en el mensaje que se envía a habhub.org
	 * los datos de las medidas y geolocalización de la sonda
	 * @author Rafa
	 * @version 1.0
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class UplinkMessage {
		@JsonProperty("f_port")
		int fport = 0;

		@JsonProperty("f_cnt")
		int fcnt = 0;

		@JsonProperty("frm_payload")
		byte[] payload = new byte[0];

		@JsonProperty("rx_metadata")
		List<RxMetadata> rxMetadata = new ArrayList<>();

		@JsonProperty("decoded_payload")
		DecodedPayload decodedPayload = new DecodedPayload();

		@JsonProperty("settings")
		Settings settings = new Settings();
	}
	/**
	 * Datos con las medidas de los sensores de la sonda
	 * @author Rafa
	 * @version 1.0
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class DecodedPayload {
		//altitud de la sonda
		@JsonProperty("analog_in_5")
		double analog5;
		//número de satélites
		@JsonProperty("analog_in_6")
		int analog6;
		@JsonProperty("analog_in_7")
		double analog7;
		@JsonProperty("gps_1")
		GPS gps = new GPS();
	}
	/**
	 * Datos de geolocalización de la sonda
	 * @author Rafa
	 * @version 1.0
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class GPS {
		@JsonProperty("latitude")
		double latitud;
		@JsonProperty("longitude")
		double longitud;
		@JsonProperty("altitude")
		double altitud;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class RxMetadata {

		@JsonProperty("gateway_ids")
		final GatewayIds gatewayIds = new GatewayIds();

		@JsonProperty("location")
		final Location location = new Location();
		
		@JsonProperty("rssi")
		int rssi;

		@JsonProperty("snr")
		int snr;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class GatewayIds {
		@JsonProperty("gateway_id")
		String gatewayId = "";

		@JsonProperty("eui")
		String eui = "";
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	final static class Location {
		@JsonProperty("latitude")
		private double latitude = Double.NaN;

		@JsonProperty("longitude")
		private double longitude = Double.NaN;

		@JsonProperty("altitude")
		private double altitude = Double.NaN;
	}
	/**
	 * Se añaden los campos con las medidas del gps y de los sensores
	 * @author Rafa
	 * @version 1.0
	 */
	@Override
	public LoraWanUplinkMessage toLoraWanUplinkMessage() {
		LoraWanUplinkMessage uplink = new LoraWanUplinkMessage("TheThingsNetwork", Instant.parse(receivedAt),
				endDeviceIds.deviceId, uplinkMessage.fcnt, uplinkMessage.fport,
				uplinkMessage.payload);
		for (RxMetadata metadata : uplinkMessage.rxMetadata) {
            String id = metadata.gatewayIds.gatewayId.trim();
            
            if (id.isEmpty()) {
                id = metadata.gatewayIds.eui;
            }
            uplink.addGateway(id, metadata.location.latitude, metadata.location.longitude, metadata.location.altitude);
        
            uplink.addField("rssi", uplinkMessage.rxMetadata.get(0).rssi);
			uplink.addField("snr", uplinkMessage.rxMetadata.get(0).snr);
		
		}
		
		//geolocalización
		if(uplinkMessage.decodedPayload!=null) {
			if(uplinkMessage.decodedPayload.gps!=null) {
				uplink.addField("latitude", uplinkMessage.decodedPayload.gps.latitud);
				uplink.addField("longitude", uplinkMessage.decodedPayload.gps.longitud);
				uplink.addField("altitude", (double)uplinkMessage.decodedPayload.analog5);
			}
			uplink.addField("altura", uplinkMessage.decodedPayload.analog5);
			uplink.addField("num. satélites", uplinkMessage.decodedPayload.analog6);

		}
		//configuración del transmisor
		if(uplinkMessage.settings!=null) {
			uplink.addField("frequency", uplinkMessage.settings.frequency);
		}
		
		return uplink;
	}

}
