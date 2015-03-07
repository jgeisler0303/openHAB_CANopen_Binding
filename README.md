# CANopen Binding for openHAB
This is the very second version of a Binding that allows the connection of [CANopen](http://en.wikipedia.org/wiki/CANopen) devices to the [openHAB server](http://www.openhab.org/) via a [socket can connection](http://en.wikipedia.org/wiki/SocketCAN).

The code was originally founded upon and partly copied from this [socket can binding](https://github.com/agriesser/socketcan-binding-lager) but has evolved quite a bit since.

# News on 2015/03/06
* The lib-socketcan-java.jar needed to access the system CAN bus driver was moved to a separate bundle hosted [here](https://github.com/jgeisler0303/openHAB_io_canbus). You must download or clone this bundle and import the project contained therein into eclipse or create a bundle .jar and copy that into the addons folder of your openHAB installation!!!
* Implemented full support for pdos, sdo (expedited only), nmt, sync
* Implemented support for all numeric CANopen data types (no strings yet)
* Started implementing tests

# Getting started
To test the binding you have to clone this repository, import the project into an [eclipse setup of openHAB](https://github.com/openhab/openhab/wiki/IDE-Setup) and make some configurations:
* Add the line ```canopen:refresh=60000``` to ```openhab/distribution/openhabhome/configurations/openhab.cfg```. Otherwise the binding will not start!!!
* Add ```-Dsocketcan.testmode=true``` to the "VM arguments" in the "Arguments" tab of eclips' "Run Configurations" of openHAB. This way you will get a fake socketcan interface that sends a series of test messages and replies to SDO messages.
* Alternativly, don't set that argument, start a virtual can device like explained in the [Wiki](http://en.wikipedia.org/wiki/SocketCAN#Usage) (you will probably have to run all the commands with ```sudo```), and send a message using ```cansend```.
* In order to map the message to an item, add this line to the ```openhab/distribution/openhabhome/configurations/items/demo.items``` file: ```Number CANopenTestHumi "test of humidity [%.1f %%]" <temperature> {canopen="if:vcan0,type:real32,txpdo:0x297,txofs:4"}``` or ```Number CANopenTestTemp "test of temperature [%.1f °C]" <temperature> {canopen="if:vcan0,type:real32,txpdo:0x297,txofs:0"}```. See example for other possible configurations in the .items file in testconfig folder.
* Now you could add this line to ```openhab/distribution/openhabhome/configurations/sitemaps/demo.sitemap```: ```Text item=CANopenTestTemp valuecolor=[>25="orange",>15="green",>5="orange",<=5="blue"]``` just below the line ```Frame label="Weather" {``` to view the contents of the new CANopen message item
* Finally, you could connet your [CANFestivino device](https://github.com/jgeisler0303/CANFestivino) to a USB-to-CAN adapter like the [USBtin](http://www.fischl.de/usbtin/) and log some real data.

That's it for now. any questions, comments or help is welcome.

# Testing
For testing the PDO and SDO functionality, copy the item and rules files from the ```testconfig``` folder into the respective folders in you openHAB configurations folder. Add ```-Dsocketcan.testmode=true``` to the "VM arguments" in the "Arguments" tab of eclips' "Run Configurations" of openHAB. And add these lines to your ```logback_debug.xml``` file.
```
	<appender name="CANopenTest" class="ch.qos.logback.core.FileAppender">
	  <file>${openhab.logdir:-logs}/TestCANopen.log</file>
	  <append>false</append>
		<encoder>
			<pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
		</encoder>
	</appender>

	<logger name="org.openhab.binding.canopen" level="DEBUG" additivity="false">
		<appender-ref ref="CANopenTest" />
	</logger>
```
Now start openHAB with the CANopen and ```org.openhab.io.transport.socketcan``` bundles set to autostart.

Upon loading the ```SDOCANopenTest_boolean``` item will be queried for the current device value and the fake testing socket will reply with a valid response. Then, after 10 seconds the fake testing socket will start sending transmit PDO messages, about five to siy different values for each data type (as configured in the item file). The test rules will then copy the received values to receive PDO items and the fake testing socket will log their correct reception. Also, the first two PDOs are copied to the configured SDO items and the fake testing socket will resond to the SDO write messages sent there upon.

You all the test are not driectily logged as success or failure like a ture unit test. But you can watch and interpret test results and going-ons in the specially generated ```TestCANopen.log``` file in you openHABs log folder.

# Future Plans
Given enough time, I plan to improve this binding like this:
* Add support for special openHAB commands and item types (e.g. Switch, Dimmer...)
* Add support for segmented SDO transfer
* Add support for CANopen errors
* Add support for sending CANopen time stamps
* Refactor the bundle into separate bundles, one for each protocol type (PDO, SDO etc.)
* Make to tool to extract item definitions from CANopen EDS files, or better yet, a special ItemProvider to directly add auto-generated items to the bus
