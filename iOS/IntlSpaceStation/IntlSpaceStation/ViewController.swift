//
//  ViewController.swift
//  IntlSpaceStation
//
//  Created by Rodolfo Aramayo on 8/3/15.
//  Copyright (c) 2015 OrbitusRobotics. All rights reserved.
//

import UIKit
import MapKit

class ViewController: UIViewController, MKMapViewDelegate {

    var issPositionURL_String = "http://api.open-notify.org/iss-now.json"
    @IBOutlet weak var placemark: UILabel?
    @IBOutlet weak var latitude: UILabel?
    @IBOutlet weak var longitude: UILabel?
    @IBOutlet weak var map: MKMapView?
    
    var old_latitude: CLLocationDegrees? = 0
    var old_longitude: CLLocationDegrees? = 0

    var spaceTimer: NSTimer?
    var issAnnotation: MKPointAnnotation?
    var routeLine: MKOverlay?
    var routeLineView: MKPolylineView?
    var firstCoord: Bool?
    var locationManager: CLLocationManager?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        map?.mapType = MKMapType.Hybrid
        map?.userInteractionEnabled = true
        map?.zoomEnabled = true
        
        firstCoord = true;
        
        pollSpaceData()
        spaceTimer = NSTimer.scheduledTimerWithTimeInterval(1, target: self, selector: Selector("pollSpaceData"), userInfo: nil, repeats: true)
        
        /*
        SEL requestSelector = NSSelectorFromString(@"requestWhenInUseAuthorization");
        if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined &&
            [self.locationManager respondsToSelector:requestSelector]) {
                [self.locationManager performSelector:requestSelector withObject:NULL];
        } else {
            [self.locationManager startUpdatingLocation];
        }*/
        finishLaunch()
    }

    func finishLaunch() {
        //ask for authorization
        self.locationManager = CLLocationManager()
        let status = CLLocationManager.authorizationStatus()
        if(status == CLAuthorizationStatus.NotDetermined) {
            self.locationManager!.requestAlwaysAuthorization();
            print("yes")
        }
        else {
            print("nope")
            //self.startMonitoring()
        }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    func pollSpaceData ()
    {
        
        // Do any additional setup after loading the view, typically from a nib.
        let issPositionURL = NSURL(string: issPositionURL_String)
        let issPositionData = NSData(contentsOfURL: issPositionURL!)
        
        let issJSONString = NSString(data: issPositionData!, encoding: NSUTF8StringEncoding)
        //print(issJSONString)
        
        let issPosition_json: AnyObject
        
        var parseError: NSError?

        issPosition_json = NSJSONSerialization.JSONObjectWithData(issPositionData!, options:  NSJSONReadingOptions.AllowFragments, error:&parseError)!
        
        //print(issJSONString)
        var latitude_components = issJSONString?.componentsSeparatedByString("latitude\": ")
        

        
        //print(issJSONString?.componentsSeparatedByString("\"latitude\": ")[1].componentsSeparatedByString(",")[0])
    
        //print(issJSONString?.componentsSeparatedByString("\"longitude\": ")[1].componentsSeparatedByString(",")[0])
    
        var lat = issJSONString?.componentsSeparatedByString("\"latitude\": ")[1].componentsSeparatedByString(",")[0] as! String
        var long = issJSONString?.componentsSeparatedByString("\"longitude\": ")[1].componentsSeparatedByString(",")[0] as! String
    
        latitude?.text = lat
        longitude?.text = long
        
        let momentaryLatitude = (lat as NSString).doubleValue
        let momentaryLongitude = (long as NSString).doubleValue
        
        
        let iss_clLocation = CLLocation(latitude: momentaryLatitude, longitude: momentaryLongitude)

        
        let regionRadius: CLLocationDistance = 1000000

        let coordinateRegion = MKCoordinateRegionMakeWithDistance(iss_clLocation.coordinate,
            regionRadius * 2.0, regionRadius * 2.0)
        if (firstCoord == true)
        {
            map!.setRegion(coordinateRegion, animated: true)
        }
        

        
        map?.removeAnnotation(issAnnotation)
        issAnnotation = MKPointAnnotation()
        
        issAnnotation!.coordinate.latitude = iss_clLocation.coordinate.latitude
        issAnnotation!.coordinate.longitude = iss_clLocation.coordinate.longitude

        issAnnotation!.title = "ISS"
        map?.addAnnotation(issAnnotation)
        
        
        var coordinateArray: [CLLocationCoordinate2D] = [CLLocationCoordinate2DMake(old_latitude!, old_longitude!),CLLocationCoordinate2DMake(iss_clLocation.coordinate.latitude, iss_clLocation.coordinate.longitude)]

        
        routeLine = MKPolyline(coordinates: &coordinateArray, count: 2)
        //Zooms into the region spanning the recorded path
        //map?.setVisibleMapRect(routeLine!.boundingMapRect, animated: true)
        if (firstCoord == false)
        {
            map?.addOverlay(routeLine)
        }
        firstCoord = false
        
        
        
        
        //---- Reverse Geocode Location
        
        var location = CLLocation(latitude: iss_clLocation.coordinate.latitude, longitude: iss_clLocation.coordinate.longitude)
        
        CLGeocoder().reverseGeocodeLocation(location, completionHandler: {(placemarks, error) -> Void in
            //println(location)
            
            if error != nil {
                println("Reverse geocoder failed with error" + error.localizedDescription)
                return
            }
            
            if placemarks.count > 0 {
                let pm = placemarks[0] as! CLPlacemark
                
                //println(pm.addressDictionary["FormattedAddressLines"]!.dynamicType)
                self.placemark?.text = pm.addressDictionary["FormattedAddressLines"]!.objectAtIndex(0) as? String
                
            }
            else {
                println("Problem with the data received from geocoder")
            }
        })
        //----
        
        
        old_latitude = iss_clLocation.coordinate.latitude
        old_longitude = iss_clLocation.coordinate.longitude
    }
    
    
    func mapView(mapView: MKMapView!, rendererForOverlay overlay: MKOverlay!) -> MKOverlayRenderer! {

        var polylineRenderer = MKPolylineRenderer(overlay: overlay)
        polylineRenderer.strokeColor = UIColor.blueColor()
        polylineRenderer.lineWidth = 5
        return polylineRenderer
    }
    
    
    func mapView(mapView: MKMapView!,
        viewForAnnotation annotation: MKAnnotation!) -> MKAnnotationView! {
            
            
            if annotation is MKUserLocation {
                return nil
            }
            
            let reuseId = "IntlSpaceStation"
            
            var pinView = mapView.dequeueReusableAnnotationViewWithIdentifier(reuseId) as? MKPinAnnotationView
            if pinView == nil {
                pinView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: reuseId)
                pinView!.canShowCallout = false
                pinView!.animatesDrop = false
                pinView!.pinColor = MKPinAnnotationColor.Red
                var imageView = UIImageView(image: UIImage(named: "spacestation_small"))
                imageView.center = CGPointMake(0.0, 0.0)
                pinView?.addSubview(imageView)
            }
            else {
                pinView!.annotation = annotation
            }
            
            return pinView
    }

}

