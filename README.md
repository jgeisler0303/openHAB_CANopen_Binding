# CANopen Binding for openHAB
This is the very fist version of a Binding that allows the connection of [CANopen](http://en.wikipedia.org/wiki/CANopen) devices to the [openHAB server](http://www.openhab.org/) via a [socket can connection](http://en.wikipedia.org/wiki/SocketCAN).

The code is heavily based upon and partly copied from this [socket can binding](https://github.com/agriesser/socketcan-binding-lager).

Currently, only reception of PDOs with ```real32``` type data is provided. But the framework is setup and can easily be extended to other data types, sending of PDOs, request and reception of SDOs, sending of sync messages etc.

To test the binding you have to clone this repository, import the project into an [eclipse setup of openHAB](https://github.com/openhab/openhab/wiki/IDE-Setup) and make some configurations:
* Add the line ```canopen:refresh=60000``` to ```openhab/distribution/openhabhome/configurations/openhab.cfg```. Otherwise the binding will not start!!!
* Add ```-Dsocketcan.testmode=true``` to the "VM arguments" in the "Arguments" tab of eclips' "Run Configurations" of openHAB. This way you will get a fake socketcan interface that periodically sends a message with id 663.
* Alternativly, don't set that argument, start a virtual can device like explained in the [Wiki](http://en.wikipedia.org/wiki/SocketCAN#Usage) (you will probably have to run all the commands with ```sudo```), and send a message like ```cansend vcan0 297#6666004233335942```.
* In order to map the message to an item, add tis line to the ```openhab/distribution/openhabhome/configurations/items/demo.items``` file: ```Number CANopenTestHumi "test of humidity [%.1f %%]" <temperature> {canopen="if:vcan0,type:real32,txpdo:0x297,txofs:4"}``` or ```Number CANopenTestTemp "test of temperature [%.1f °C]" <temperature> {canopen="if:vcan0,type:real32,txpdo:0x297,txofs:0"}```.
* Now you could add this line to ```openhab/distribution/openhabhome/configurations/sitemaps/demo.sitemap```: ```Text item=CANopenTestTemp valuecolor=[>25="orange",>15="green",>5="orange",<=5="blue"]``` just the line ```Frame label="Weather" {``` to view the contents of the CANopen message
* Finally, you could connet your [CANFestivino device](https://github.com/jgeisler0303/CANFestivino) to a USB-to-CAN adapter like the [USBtin](http://www.fischl.de/usbtin/) and log some real data.

That's it for now. any questions, comments or help is welcome.

# Future Plans
Given enough time, I plan to improve this binding like this:
* Add support for all data types
* Add support for sending PDOs
* Add support for SDOs
* Add support for CANopen network management
* Add support for node guarding and handling of CANopen errors
* Make to tool to extract item definitions from CANopen EDS files, or better yet, a special ItemProvider to directly add auto-generated items to the bus
